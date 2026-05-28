package com.lalema.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lalema.backend.entity.FriendRequest;
import com.lalema.backend.entity.Friendship;
import com.lalema.backend.entity.PoopRecord;
import com.lalema.backend.entity.User;
import com.lalema.backend.mapper.FriendRequestMapper;
import com.lalema.backend.mapper.FriendshipMapper;
import com.lalema.backend.mapper.PoopRecordMapper;
import com.lalema.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendRequestMapper requestMapper;
    private final FriendshipMapper friendshipMapper;
    private final UserMapper userMapper;
    private final PoopRecordMapper recordMapper;

    public List<Map<String, Object>> searchUsers(Long userId, String keyword) {
        List<User> users = userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .ne(User::getId, userId)
                .and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword))
                .last("LIMIT 20")
        );
        Set<Long> friendIds = getFriendIds(userId);
        return users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", u.getId());
            map.put("username", u.getUsername());
            map.put("nickname", u.getNickname() != null ? u.getNickname() : u.getUsername());
            map.put("isFriend", friendIds.contains(u.getId()));
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void sendRequest(Long senderId, Long receiverId, String message) {
        if (senderId.equals(receiverId)) throw new RuntimeException("不能添加自己为好友");
        if (isFriend(senderId, receiverId)) throw new RuntimeException("已经是好友了");
        FriendRequest existing = requestMapper.selectOne(
            new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getSenderId, senderId)
                .eq(FriendRequest::getReceiverId, receiverId)
                .eq(FriendRequest::getStatus, "PENDING")
        );
        if (existing != null) throw new RuntimeException("已发送过请求，请等待对方处理");
        FriendRequest reverse = requestMapper.selectOne(
            new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getSenderId, receiverId)
                .eq(FriendRequest::getReceiverId, senderId)
                .eq(FriendRequest::getStatus, "PENDING")
        );
        if (reverse != null) {
            reverse.setStatus("ACCEPTED");
            requestMapper.updateById(reverse);
            addFriendship(senderId, receiverId);
            return;
        }
        FriendRequest req = new FriendRequest();
        req.setSenderId(senderId);
        req.setReceiverId(receiverId);
        req.setMessage(message);
        req.setStatus("PENDING");
        requestMapper.insert(req);
    }

    @Transactional
    public void acceptRequest(Long userId, Long requestId) {
        FriendRequest req = requestMapper.selectById(requestId);
        if (req == null || !req.getReceiverId().equals(userId)) throw new RuntimeException("请求不存在");
        if (!"PENDING".equals(req.getStatus())) throw new RuntimeException("请求已处理");
        req.setStatus("ACCEPTED");
        requestMapper.updateById(req);
        addFriendship(req.getSenderId(), req.getReceiverId());
    }

    public void rejectRequest(Long userId, Long requestId) {
        FriendRequest req = requestMapper.selectById(requestId);
        if (req == null || !req.getReceiverId().equals(userId)) throw new RuntimeException("请求不存在");
        req.setStatus("REJECTED");
        requestMapper.updateById(req);
    }

    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        friendshipMapper.delete(new LambdaQueryWrapper<Friendship>()
            .eq(Friendship::getUserId, userId).eq(Friendship::getFriendId, friendId));
        friendshipMapper.delete(new LambdaQueryWrapper<Friendship>()
            .eq(Friendship::getUserId, friendId).eq(Friendship::getFriendId, userId));
    }

    public List<Map<String, Object>> getFriends(Long userId) {
        List<Friendship> friendships = friendshipMapper.selectList(
            new LambdaQueryWrapper<Friendship>().eq(Friendship::getUserId, userId)
        );
        List<Long> friendIds = friendships.stream().map(Friendship::getFriendId).collect(Collectors.toList());
        if (friendIds.isEmpty()) return Collections.emptyList();
        List<User> friends = userMapper.selectBatchIds(friendIds);
        Map<Long, Friendship> remarkMap = friendships.stream()
            .collect(Collectors.toMap(Friendship::getFriendId, f -> f));
        return friends.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", u.getId());
            map.put("username", u.getUsername());
            map.put("nickname", u.getNickname() != null ? u.getNickname() : u.getUsername());
            Friendship fs = remarkMap.get(u.getId());
            map.put("remark", fs != null && fs.getRemark() != null ? fs.getRemark() : "");
            return map;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPendingRequests(Long userId) {
        List<FriendRequest> requests = requestMapper.selectList(
            new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getReceiverId, userId)
                .eq(FriendRequest::getStatus, "PENDING")
                .orderByDesc(FriendRequest::getCreatedAt)
        );
        List<Long> senderIds = requests.stream().map(FriendRequest::getSenderId).collect(Collectors.toList());
        if (senderIds.isEmpty()) return Collections.emptyList();
        List<User> senders = userMapper.selectBatchIds(senderIds);
        Map<Long, User> senderMap = senders.stream().collect(Collectors.toMap(User::getId, u -> u));
        return requests.stream().map(r -> {
            User sender = senderMap.get(r.getSenderId());
            Map<String, Object> map = new HashMap<>();
            map.put("requestId", r.getId());
            map.put("senderId", r.getSenderId());
            map.put("username", sender != null ? sender.getUsername() : "");
            map.put("nickname", sender != null && sender.getNickname() != null ? sender.getNickname() : "");
            map.put("message", r.getMessage());
            map.put("createdAt", r.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
    }

    public int getPendingCount(Long userId) {
        return requestMapper.selectCount(
            new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getReceiverId, userId)
                .eq(FriendRequest::getStatus, "PENDING")
        ).intValue();
    }

    public List<Map<String, Object>> getLeaderboard(Long userId) {
        Set<Long> friendIds = getFriendIds(userId);
        friendIds.add(userId);
        List<User> users = userMapper.selectBatchIds(friendIds);
        String monthPattern = String.format("%04d-%02d-%%", YearMonth.now().getYear(), YearMonth.now().getMonthValue());
        return users.stream().map(u -> {
            int monthRecords = recordMapper.selectCount(
                new LambdaQueryWrapper<PoopRecord>()
                    .eq(PoopRecord::getUserId, u.getId())
                    .likeRight(PoopRecord::getDate, monthPattern)
            ).intValue();
            Map<String, Object> map = new HashMap<>();
            map.put("userId", u.getId());
            map.put("nickname", u.getNickname() != null ? u.getNickname() : u.getUsername());
            map.put("monthRecords", monthRecords);
            map.put("isMe", u.getId().equals(userId));
            return map;
        }).sorted((a, b) -> Integer.compare((int) b.get("monthRecords"), (int) a.get("monthRecords")))
          .collect(Collectors.toList());
    }

    public Map<String, Object> getFriendStats(Long friendId) {
        User user = userMapper.selectById(friendId);
        if (user == null) throw new RuntimeException("用户不存在");
        String monthPattern = String.format("%04d-%02d-%%", YearMonth.now().getYear(), YearMonth.now().getMonthValue());
        int monthRecords = recordMapper.selectCount(
            new LambdaQueryWrapper<PoopRecord>()
                .eq(PoopRecord::getUserId, friendId)
                .likeRight(PoopRecord::getDate, monthPattern)
        ).intValue();
        int totalRecords = recordMapper.selectCount(
            new LambdaQueryWrapper<PoopRecord>().eq(PoopRecord::getUserId, friendId)
        ).intValue();
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getId());
        map.put("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
        map.put("monthRecords", monthRecords);
        map.put("totalRecords", totalRecords);
        return map;
    }

    private void addFriendship(Long userId, Long friendId) {
        Friendship f1 = new Friendship();
        f1.setUserId(userId);
        f1.setFriendId(friendId);
        friendshipMapper.insert(f1);
        Friendship f2 = new Friendship();
        f2.setUserId(friendId);
        f2.setFriendId(userId);
        friendshipMapper.insert(f2);
    }

    private boolean isFriend(Long userId, Long friendId) {
        return friendshipMapper.selectCount(
            new LambdaQueryWrapper<Friendship>()
                .eq(Friendship::getUserId, userId).eq(Friendship::getFriendId, friendId)
        ) > 0;
    }

    private Set<Long> getFriendIds(Long userId) {
        return friendshipMapper.selectList(
            new LambdaQueryWrapper<Friendship>().eq(Friendship::getUserId, userId)
        ).stream().map(Friendship::getFriendId).collect(Collectors.toSet());
    }
}

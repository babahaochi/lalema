package com.lalema.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lalema.backend.dto.FriendRequestDTO;
import com.lalema.backend.dto.FriendUserDTO;
import com.lalema.backend.dto.LeaderboardItemDTO;
import com.lalema.backend.entity.FriendRequest;
import com.lalema.backend.entity.Friendship;
import com.lalema.backend.entity.PoopRecord;
import com.lalema.backend.entity.User;
import com.lalema.backend.mapper.FriendRequestMapper;
import com.lalema.backend.mapper.FriendshipMapper;
import com.lalema.backend.mapper.NotificationMapper;
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
    private final NotificationMapper notificationMapper;

    public List<FriendUserDTO> searchUsers(Long userId, String keyword) {
        List<User> users = userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .ne(User::getId, userId)
                .and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword))
                .last("LIMIT 20")
        );
        Set<Long> friendIds = getFriendIds(userId);
        return users.stream().map(u -> {
            FriendUserDTO dto = new FriendUserDTO();
            dto.setUserId(u.getId());
            dto.setUsername(u.getUsername());
            dto.setNickname(u.getNickname() != null ? u.getNickname() : u.getUsername());
            dto.setIsFriend(friendIds.contains(u.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    private static final int MAX_MESSAGE_LENGTH = 200;

    @Transactional
    public void sendRequest(Long senderId, Long receiverId, String message) {
        if (senderId.equals(receiverId)) throw new RuntimeException("不能添加自己为好友");
        if (isFriend(senderId, receiverId)) throw new RuntimeException("已经是好友了");

        String sanitizedMessage = null;
        if (message != null) {
            String trimmed = message.trim();
            if (trimmed.length() > MAX_MESSAGE_LENGTH) {
                trimmed = trimmed.substring(0, MAX_MESSAGE_LENGTH);
            }
            sanitizedMessage = trimmed.replaceAll("[<>\"'&]", "");
        }

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
        req.setMessage(sanitizedMessage);
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

    public List<FriendUserDTO> getFriends(Long userId) {
        List<Friendship> friendships = friendshipMapper.selectList(
            new LambdaQueryWrapper<Friendship>().eq(Friendship::getUserId, userId)
        );
        List<Long> friendIds = friendships.stream().map(Friendship::getFriendId).collect(Collectors.toList());
        if (friendIds.isEmpty()) return Collections.emptyList();
        List<User> friends = userMapper.selectBatchIds(friendIds);
        Map<Long, Friendship> remarkMap = friendships.stream()
            .collect(Collectors.toMap(Friendship::getFriendId, f -> f));
        return friends.stream().map(u -> {
            FriendUserDTO dto = new FriendUserDTO();
            dto.setUserId(u.getId());
            dto.setUsername(u.getUsername());
            dto.setNickname(u.getNickname() != null ? u.getNickname() : u.getUsername());
            Friendship fs = remarkMap.get(u.getId());
            dto.setRemark(fs != null && fs.getRemark() != null ? fs.getRemark() : "");
            return dto;
        }).collect(Collectors.toList());
    }

    public List<FriendRequestDTO> getPendingRequests(Long userId) {
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
            FriendRequestDTO dto = new FriendRequestDTO();
            dto.setRequestId(r.getId());
            dto.setSenderId(r.getSenderId());
            dto.setUsername(sender != null ? sender.getUsername() : "");
            dto.setNickname(sender != null && sender.getNickname() != null ? sender.getNickname() : "");
            dto.setMessage(r.getMessage());
            dto.setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : "");
            return dto;
        }).collect(Collectors.toList());
    }

    public int getPendingCount(Long userId) {
        return requestMapper.selectCount(
            new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getReceiverId, userId)
                .eq(FriendRequest::getStatus, "PENDING")
        ).intValue();
    }

    public List<LeaderboardItemDTO> getLeaderboard(Long userId) {
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
            LeaderboardItemDTO dto = new LeaderboardItemDTO();
            dto.setUserId(u.getId());
            dto.setNickname(u.getNickname() != null ? u.getNickname() : u.getUsername());
            dto.setMonthRecords(monthRecords);
            dto.setIsMe(u.getId().equals(userId));
            return dto;
        }).sorted((a, b) -> Integer.compare(b.getMonthRecords(), a.getMonthRecords()))
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

    public void remindFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) throw new RuntimeException("不能提醒自己");
        if (!isFriend(userId, friendId)) throw new RuntimeException("对方不是你的好友");

        com.lalema.backend.entity.User sender = userMapper.selectById(userId);
        if (sender == null) throw new RuntimeException("用户不存在");

        com.lalema.backend.entity.Notification notification = new com.lalema.backend.entity.Notification();
        notification.setUserId(friendId);
        notification.setType("REMIND");
        notification.setTitle("好友提醒");
        notification.setContent(sender.getNickname() != null ? sender.getNickname() : sender.getUsername() + " 提醒你记得打卡哦~");
        notification.setRelatedId(userId);
        notification.setIsRead(false);
        notificationMapper.insert(notification);
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

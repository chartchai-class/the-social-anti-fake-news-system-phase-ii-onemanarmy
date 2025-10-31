package se331.backend.security.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se331.backend.entity.UserAuthDTO; // ⭐️ Import DTO
import se331.backend.util.NewsMapper;   // ⭐️ Import Mapper
import java.util.List;
import java.util.stream.Collectors; // ⭐️ Import Stream

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    final UserDao userDao;
    final NewsMapper newsMapper;
    private User savedUser;

    @Override
    @Transactional
    public User save(User user) {
        return userDao.save(user);
    }

    @Override
    @Transactional
    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    @Transactional
    public User findById(Integer id) {
        return userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Admin Controller
    @Override
    @Transactional
    public List<UserAuthDTO> getAllUsers() {
        return userDao.findAll().stream()
                .map(newsMapper::toUserAuthDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserAuthDTO promoteUserToMember(Integer userId) {
        User user = findById(userId);
        if (!user.getRoles().contains(Role.ROLE_MEMBER)) {
            user.getRoles().add(Role.ROLE_MEMBER);
            user.getRoles().remove(Role.ROLE_READER); // (ลบ Role เก่าออก)
        }
        User savedUser = userDao.save(user);
        return newsMapper.toUserAuthDTO(savedUser);
    }

    @Override
    @Transactional
    public UserAuthDTO demoteUserToReader(Integer userId) {
        User user = findById(userId); // (ใช้ findById ที่มีอยู่)

        // ตรวจสอบว่าเป็น MEMBER (และไม่ใช่ ADMIN) หรือไม่
        if (user.getRoles().contains(Role.ROLE_MEMBER) && !user.getRoles().contains(Role.ROLE_ADMIN)) {
            user.getRoles().remove(Role.ROLE_MEMBER); // ลบ MEMBER ออก
            if (!user.getRoles().contains(Role.ROLE_READER)) { // เพิ่ม READER ถ้ายังไม่มี (เผื่อกรณี Role อื่นๆ)
                user.getRoles().add(Role.ROLE_READER);
            }
        } else if (user.getRoles().contains(Role.ROLE_ADMIN)) {
            // ป้องกันการลดขั้น Admin 
            throw new IllegalArgumentException("Cannot demote an ADMIN user.");
        }
        User savedUser = userDao.save(user);
        return newsMapper.toUserAuthDTO(savedUser);

    }
}
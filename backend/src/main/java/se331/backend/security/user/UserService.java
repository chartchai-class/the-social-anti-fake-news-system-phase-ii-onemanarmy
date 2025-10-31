package se331.backend.security.user;

import jakarta.transaction.Transactional;
import se331.backend.entity.UserAuthDTO; // ⭐️ Import DTO
import java.util.List;

public interface UserService {
    User save(User user);

    @Transactional
    User findByUsername(String username);

    @Transactional
    User findById(Integer id);

    @Transactional
    List<UserAuthDTO> getAllUsers();

    @Transactional
    UserAuthDTO promoteUserToMember(Integer userId);

    @Transactional
    UserAuthDTO demoteUserToReader(Integer userId);
}
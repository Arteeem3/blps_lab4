package itmo.blps.config;

import itmo.blps.entity.User;
import itmo.blps.entity.UserRole;
import itmo.blps.repository.UserRepository;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.Group;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CamundaIdentityConfig {

    @Bean
    public CommandLineRunner syncIdentity(IdentityService identityService, UserRepository userRepository) {
        return args -> {
            createGroupIfNotExists(identityService, "camunda-admin", "Camunda Admins");
            createGroupIfNotExists(identityService, UserRole.SELLER.name(), "Продавцы");
            createGroupIfNotExists(identityService, UserRole.BUYER.name(), "Покупатели");
            createGroupIfNotExists(identityService, UserRole.ADMIN.name(), "Администраторы");

            List<User> users = userRepository.findAll();
            for (User u : users) {
                String userId = String.valueOf(u.getId());
                String email = u.getEmail();

                org.camunda.bpm.engine.identity.User cUser = identityService.createUserQuery().userId(userId).singleResult();
                if (cUser == null) {
                    cUser = identityService.newUser(userId);
                    cUser.setEmail(email);
                    cUser.setFirstName(email.split("@")[0]);
                    cUser.setLastName(u.getRole().name());
                    cUser.setPassword(userId);
                    identityService.saveUser(cUser);
                }

                String groupId = u.getRole().name();
                createMembershipIfNotExists(identityService, userId, groupId);
                if (u.getRole() == UserRole.ADMIN) {
                    createMembershipIfNotExists(identityService, userId, "camunda-admin");
                }
            }
        };
    }

    private void createGroupIfNotExists(IdentityService identityService, String groupId, String groupName) {
        if (identityService.createGroupQuery().groupId(groupId).singleResult() == null) {
            Group group = identityService.newGroup(groupId);
            group.setName(groupName);
            group.setType("WORKFLOW");
            identityService.saveGroup(group);
        }
    }

    private void createMembershipIfNotExists(IdentityService identityService, String userId, String groupId) {
        boolean exists = identityService.createUserQuery()
                .userId(userId)
                .memberOfGroup(groupId)
                .singleResult() != null;
        if (!exists) {
            try {
                identityService.createMembership(userId, groupId);
            } catch (Exception ignored) {
            }
        }
    }
}

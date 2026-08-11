package com.archcore.core.service;

import com.archcore.core.domain.UserProfile;
import com.archcore.core.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional(readOnly = true)
    public UserProfile getOrCreateProfile(String userId, String firstName, String lastName) {
        return userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile(userId, firstName, lastName);
                    return userProfileRepository.save(newProfile);
                });
    }

    @Transactional(readOnly = true)
    public UserProfile getProfile(String userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user: " + userId));
    }

    public UserProfile updateProfile(String userId, String firstName, String lastName, String profilePictureUrl) {
        UserProfile profile = getProfile(userId);

        if (firstName != null) {
            profile.setFirstName(firstName);
        }
        if (lastName != null) {
            profile.setLastName(lastName);
        }
        if (profilePictureUrl != null) {
            profile.setProfilePictureUrl(profilePictureUrl);
        }

        UserProfile updated = userProfileRepository.save(profile);
        log.info("Updated profile for user: {}", userId);
        return updated;
    }

    public void deactivateAccount(String userId, String reason) {
        UserProfile profile = getProfile(userId);
        profile.setAccountActive(false);
        profile.setDeactivationReason(reason);
        userProfileRepository.save(profile);
        log.info("Deactivated account for user: {}", userId);
    }

    public void reactivateAccount(String userId) {
        UserProfile profile = getProfile(userId);
        profile.setAccountActive(true);
        profile.setDeactivationReason(null);
        userProfileRepository.save(profile);
        log.info("Reactivated account for user: {}", userId);
    }

    public boolean isAccountActive(String userId) {
        return userProfileRepository.findByUserId(userId)
                .map(UserProfile::isAccountActive)
                .orElse(true);
    }
}

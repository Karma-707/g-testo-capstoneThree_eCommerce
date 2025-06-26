package org.yearup.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;
import org.yearup.models.User;

import java.security.Principal;

@RestController
@RequestMapping("profile")
@CrossOrigin
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private ProfileDao profileDao;
    private UserDao userDao;

    @Autowired
    public ProfileController(ProfileDao profileDao, UserDao userDao) {
        this.profileDao = profileDao;
        this.userDao = userDao;
    }

    @GetMapping("")
    public Profile getProfile(Principal principal) {
        // get the currently logged in username
        String userName = principal.getName();
        logger.info("Fetching profile for user: {}", userName);

        // find database user by userId
        User user = userDao.getByUserName(userName);
        int userId = user.getId();

        return profileDao.getByUserId(userId);
    }

    @PutMapping("")
    public Profile updateProfile(@RequestBody Profile profile, Principal principal) {
        // get the currently logged in username
        String userName = principal.getName();
        logger.info("Updating profile for user: {}", userName);

        // find database user by userId
        User user = userDao.getByUserName(userName);
        int userId = user.getId();

        profile.setUserId(userId); //set userId before updating
        profileDao.update(profile);

        return profile;
    }

}

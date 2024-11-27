package hr.combis.application.api.controller;

import hr.combis.application.data.model.User;
import hr.combis.application.security.AuthenticatedUser;
import hr.combis.application.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/api/v1/user")
public class FileUploadController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticatedUser authenticatedUser;

    @PostMapping("upload-profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        Optional<User> userOpt = authenticatedUser.get();
        if (userOpt.isPresent()) {
            try {
                byte[] bytes = file.getBytes();
                userService.updateProfilePicture(userOpt.get().getId(), bytes);
                return ResponseEntity.ok().build();
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading file");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
    }
}

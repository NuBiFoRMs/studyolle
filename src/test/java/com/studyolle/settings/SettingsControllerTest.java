package com.studyolle.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.WithAccount;
import com.studyolle.account.AccountRepository;
import com.studyolle.account.AccountService;
import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.settings.form.TagForm;
import com.studyolle.settings.form.ZoneForm;
import com.studyolle.tag.TagRepository;
import com.studyolle.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    ZoneRepository zoneRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
    }

    @DisplayName("????????? ?????? ???")
    @Test
    @WithAccount("nickname")
    void updateProfileForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @DisplayName("????????? ???????????? - ????????? ??????")
    @Test
    @WithAccount("nickname")
    void updateProfile() throws Exception {
        String bio = "?????? ????????? ???????????? ??????.";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname("nickname");
        assertEquals(bio, account.getBio());
    }

    @DisplayName("????????? ???????????? - ????????? ??????")
    @Test
    @WithAccount("nickname")
    void updateProfile_error() throws Exception {
        String bio = "?????? ????????? ???????????? ??????. ?????? ????????? ???????????? ??????. ?????? ????????? ???????????? ??????. ?????? ????????? ???????????? ??????. ?????? ????????? ???????????? ??????. ?????? ????????? ???????????? ??????.";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account account = accountRepository.findByNickname("nickname");
        assertNull(account.getBio());
    }

    @DisplayName("???????????? ?????? ???")
    @Test
    @WithAccount("nickname")
    void updatePasswordForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @DisplayName("???????????? ???????????? - ????????? ??????")
    @Test
    @WithAccount("nickname")
    void updatePassword() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword", "12345678")
                .param("newPasswordConfirm", "12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname("nickname");
        assertTrue(passwordEncoder.matches("12345678", account.getPassword()));
    }

    @DisplayName("???????????? ???????????? - ????????? ??????")
    @Test
    @WithAccount("nickname")
    void updatePassword_error() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword", "12345678")
                .param("newPasswordConfirm", "11111111")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().hasErrors());
    }

    @DisplayName("?????? ?????? ???")
    @Test
    @WithAccount("nickname")
    void updateTagsForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_TAGS_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @DisplayName("?????? ???????????? - ????????? ??????")
    @Test
    @WithAccount("nickname")
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag").orElse(null);
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname("nickname").getTags().contains(newTag));
    }

    @DisplayName("?????? ???????????? - ????????? ??????")
    @Test
    @WithAccount("nickname")
    void removeTag() throws Exception {
        Account nickname = accountRepository.findByNickname("nickname");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(nickname, newTag);

        assertTrue(nickname.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(nickname.getTags().contains(newTag));
    }

    @DisplayName("?????? ?????? ???")
    @Test
    @WithAccount("nickname")
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_ZONES_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_ZONES_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @DisplayName("?????? ???????????? - ????????? ??????")
    @Test
    @WithAccount("nickname")
    void addZone() throws Exception {
        Zone testZone = Zone.builder()
                .city("test")
                .localNameOfCity("????????????")
                .province("????????????")
                .build();
        zoneRepository.save(testZone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(SettingsController.SETTINGS_ZONES_URL + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince()).orElse(null);
        assertTrue(accountRepository.findByNickname("nickname").getZones().contains(zone));
    }

    @DisplayName("?????? ???????????? - ????????? ??????")
    @Test
    @WithAccount("nickname")
    void removeZone() throws Exception {
        Account nickname = accountRepository.findByNickname("nickname");
        Zone testZone = Zone.builder()
                .city("test")
                .localNameOfCity("????????????")
                .province("????????????")
                .build();
        Zone newZone = zoneRepository.save(testZone);
        accountService.addZone(nickname, newZone);

        assertTrue(nickname.getZones().contains(newZone));

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(SettingsController.SETTINGS_ZONES_URL + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(nickname.getTags().contains(newZone));
    }
}
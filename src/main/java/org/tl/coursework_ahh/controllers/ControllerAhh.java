package org.tl.coursework_ahh.controllers;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tl.coursework_ahh.models.History;
import org.tl.coursework_ahh.models.User;
import org.tl.coursework_ahh.repositories.HistoryRepository;
import org.tl.coursework_ahh.repositories.UserRepository;
import org.tl.coursework_ahh.services.AITrackAnalysisService;
import org.tl.coursework_ahh.services.TrackService;
import org.tl.coursework_ahh.services.UserService;
import org.tl.coursework_ahh.models.Track;

import java.io.IOException;
import java.util.List;

@Controller
public class ControllerAhh {
    private static final String HOMEPAGE = "/homepage", LOG_IN = "/log-in", SIGN_UP = "/sign-up",
            INDEX = "/index", RESULTS = "/results";
    private final static String REDIRECT = "redirect:";
    private final static String LOGGED_IN_USER = "loggedInUser";

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrackService trackService;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private AITrackAnalysisService aiTrackAnalysisService;

    private List<User> userList;
    private List<History> historyList;

    //////////////////////////////////////////////////

    @PostConstruct
    public void init() {
        userList = userRepository.findAll();
        historyList = historyRepository.findAll();
    }


    @GetMapping("/")
    public String getIndex() {
        return INDEX;
    }

    @GetMapping(HOMEPAGE)
    public String getHomepage(HttpSession session) {
        if (session.isNew()) {
            return INDEX;
        }
        return HOMEPAGE;
    }

    @GetMapping(LOG_IN)
    public String getLogIn() {
        return LOG_IN;
    }

    @PostMapping(HOMEPAGE)
    public String postHomepage(@RequestParam("searchInput") String searchInput,
                               HttpSession session, RedirectAttributes redirectAttributes)
            throws IOException, InterruptedException {

        if (session.isNew()) {
            return INDEX;
        }



        System.out.println("searching iTunes...");
        Track inputTrack = trackService.searchInITunes(searchInput);


        History history = new History(userService.getIdByUsername(String.valueOf(session.getAttribute(LOGGED_IN_USER)), userList), searchInput);

        System.out.println("got track: " + inputTrack.getTrackName());

        String similarTo = "'" + inputTrack.getTrackName() + "' by " + inputTrack.getArtistName();
        redirectAttributes.addAttribute("similarTo", similarTo);

        List<Track> resultList = aiTrackAnalysisService.getFullParametersTrackList(searchInput);
        redirectAttributes.addFlashAttribute("tracks", resultList); // Use addFlashAttribute for objects

        return REDIRECT + RESULTS;
    }

    @GetMapping(RESULTS)
    public String getResults(@RequestParam("similarTo") String similarTo,
                             Model model, HttpSession session) {

        if (session.isNew()) {
            return INDEX;
        }
        model.addAttribute("similarTo", similarTo);

        return RESULTS;
    }

    @GetMapping(SIGN_UP)
    public String getSignUp() {
        return SIGN_UP;
    }

    @PostMapping(SIGN_UP)
    public String postSignUp(@RequestParam("username") String username, @RequestParam("email") String email,
                             @RequestParam("password") String password, HttpSession session) {

        if (!userService.usernameExists(username, userList) && userService.passwordIsValid(password)) {
            User user = new User(email, username, password);
            userRepository.save(user);
            userList.add(user);
            session.setAttribute(LOGGED_IN_USER, username);
            return REDIRECT + HOMEPAGE;
        }

        return SIGN_UP;
    }

    @PostMapping(LOG_IN)
    public String login(@RequestParam("username") String username, @RequestParam("password") String password,
                        HttpSession session) {


        if (userService.userExists(username, password, userList)) {
            session.setAttribute(LOGGED_IN_USER, username);
            return REDIRECT + HOMEPAGE;
        }

        return LOG_IN;
    }

}

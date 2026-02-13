import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UsernameChecker {

    // Thread-safe maps for username lookup and frequency tracking
    private final ConcurrentHashMap<String, Integer> usernames; // username -> userId
    private final ConcurrentHashMap<String, Integer> attemptFrequency; // username -> attempts

    public UsernameChecker() {
        usernames = new ConcurrentHashMap<>();
        attemptFrequency = new ConcurrentHashMap<>();
    }

    // Check if username is available (true if not taken)
    public boolean checkAvailability(String username) {
        // Increment attempt count atomically
        attemptFrequency.merge(username, 1, Integer::sum);

        // Return true if username NOT present (available)
        return !usernames.containsKey(username);
    }

    // Suggest alternatives if username is taken
    public List<String> suggestAlternatives(String username, int maxSuggestions) {
        List<String> suggestions = new ArrayList<>();
        int suffix = 1;

        // Try appending numbers to username
        while (suggestions.size() < maxSuggestions) {
            String alternative = username + suffix;
            if (!usernames.containsKey(alternative)) {
                suggestions.add(alternative);
            }
            suffix++;
        }

        // Also try a variation replacing underscores with dots
        String dotVersion = username.replace('_', '.');
        if (!usernames.containsKey(dotVersion) && suggestions.size() < maxSuggestions) {
            suggestions.add(dotVersion);
        }

        return suggestions;
    }

    // Get the username with the highest number of attempts
    public String getMostAttempted() {
        return attemptFrequency.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // Add a username to simulate taken usernames (like during registration)
    public void registerUser(String username, int userId) {
        usernames.put(username, userId);
    }

    public static void main(String[] args) {
        UsernameChecker checker = new UsernameChecker();

        // Simulate some taken usernames
        checker.registerUser("john_doe", 101);
        checker.registerUser("admin", 1);

        // Check availability
        System.out.println("Is 'john_doe' available? " + checker.checkAvailability("john_doe")); // false
        System.out.println("Is 'jane_smith' available? " + checker.checkAvailability("jane_smith")); // true

        // Suggest alternatives for taken username
        System.out.println("Suggestions for 'john_doe': " + checker.suggestAlternatives("john_doe", 5));

        // Simulate attempts to track popularity
        for (int i = 0; i < 10543; i++) {
            checker.checkAvailability("admin");
        }
        for (int i = 0; i < 3000; i++) {
            checker.checkAvailability("john_doe");
        }

        System.out.println("Most attempted username: " + checker.getMostAttempted()); // admin
    }
}

package uk.ac.shef.wit.simmetrics.similaritymetrics;

public class Levenshtein extends AbstractStringMetric {

    @Override
    public float getSimilarity(String first, String second) {
        if (first == null || second == null) return 0f;
        int distance = computeLevenshteinDistance(first, second);
        int maxLen = Math.max(first.length(), second.length());
        if (maxLen == 0) return 1f;
        return 1f - ((float) distance / maxLen);
    }

    private int computeLevenshteinDistance(String s, String t) {
        int m = s.length(), n = t.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = s.charAt(i - 1) == t.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[m][n];
    }

    @Override
    public String getLongDescriptionString() {
        return "Levenshtein edit distance similarity metric";
    }

    @Override
    public String getShortDescriptionString() {
        return "Levenshtein";
    }
}

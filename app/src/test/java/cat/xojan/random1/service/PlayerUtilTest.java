package cat.xojan.random1.service;

import org.junit.Test;

import cat.xojan.random1.feature.mediaplayback.PlayerUtil;

import static org.junit.Assert.assertEquals;

public class PlayerUtilTest {

    @Test
    public void should_convert_milliseconds_to_time() {
        // Given 1 hour in milliseconds
        int milliseconds = 1000 * 60 * 60;

        // When we convert it to a readable time
        String duration = PlayerUtil.millisToDuration(milliseconds);

        // Then we get 1 hour
        assertEquals(duration, "01:00:00");
    }

    @Test
    public void should_convert_milliseconds_to_time_without_hour_symbol() {
        // Given 59 minutes in milliseconds
        int milliseconds = 1000 * 60 * 59;

        // When we convert it to a readable time
        String duration = PlayerUtil.millisToDuration(milliseconds);

        // Then we get 59 minutes without the hour units
        assertEquals(duration, "59:00");
    }

    @Test
    public void should_return_percentage() {
        // Given the current progress and the total duration
        int currentProgress = 1730658;
        long totalDuration = 3599904;

        // When we asked for progress
        int percentage = PlayerUtil.getProgressPercentage(currentProgress, totalDuration);

        // Then we get the percentage
        assertEquals(percentage, 48);
    }

    @Test
    public void should_convert_progress_to_time() {
        // Given the progress and the total duration
        int percentageProgress = 66;
        int totalDuration = 3599904;

        // When we asked to convert it
        int duration = PlayerUtil.progressToTimer(percentageProgress, totalDuration);

        // Then we get the time in milliseconds
        assertEquals(duration, 2375000);
    }
}

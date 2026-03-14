package com.projectgoth.fusion.reputation.file.comparator;

import com.projectgoth.fusion.reputation.file.ScoreSummary;

public class ScoreSummaryByScoreFileEntryComparator extends SingleIndexIntegerFileEntryComparator {
   public ScoreSummaryByScoreFileEntryComparator() {
      super(ScoreSummary.TOTAL_SCORE_INDEX);
   }
}

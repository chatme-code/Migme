package com.projectgoth.fusion.reputation.file.comparator;

import com.projectgoth.fusion.reputation.file.ScoreSummary;

public class ScoreSummaryByScoreStringListComparator extends SingleIndexIntegerStringListComparator {
   public ScoreSummaryByScoreStringListComparator() {
      super(ScoreSummary.TOTAL_SCORE_INDEX);
   }
}

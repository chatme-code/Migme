package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class RewardProgramProcessorMappingData implements Serializable {
   private static final long serialVersionUID = 1L;
   RewardProgramData.TypeEnum rewardProgramType;
   List<String> mapping;

   public RewardProgramProcessorMappingData(RewardProgramData.TypeEnum programType) {
      this.rewardProgramType = programType;
      this.mapping = new LinkedList();
   }

   public void addProcessorClass(String processorClassName) {
      this.mapping.add(processorClassName);
   }

   public RewardProgramData.TypeEnum getProgramType() {
      return this.rewardProgramType;
   }

   public List<String> getProcessorList() {
      return this.mapping;
   }
}

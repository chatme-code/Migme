package com.projectgoth.fusion.recommendation.collector;

import com.projectgoth.fusion.slice.CollectedDataIce;
import java.util.Collection;

public interface ICollectorTransformation<TLoggable> {
   String getName();

   Collection<TLoggable> toLoggables(CollectedDataIce var1) throws CollectorTransformationException;
}

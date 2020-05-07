package org.heigit.ohsome.ohsomeapi.oshdb;

import org.heigit.bigspatialdata.oshdb.api.generic.function.SerializableSupplier;
import org.heigit.bigspatialdata.oshdb.util.celliterator.LazyEvaluatedObject;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;

/**
 * A tag-translator that can be used in map/flatMap/aggregateBy functions that may be executed on
 * remote machines.
 */
public class RemoteTagTranslator extends LazyEvaluatedObject<TagTranslator> {

  public RemoteTagTranslator(SerializableSupplier<TagTranslator> evaluator) {
    super(evaluator);
  }

  public RemoteTagTranslator(TagTranslator value) {
    super(value);
  }

  @Override
  public synchronized TagTranslator get() {
    return super.get();
  }
}

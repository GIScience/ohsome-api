package org.heigit.ohsome.ohsomeapi.oshdb;

import org.heigit.ohsome.oshdb.util.celliterator.LazyEvaluatedObject;
import org.heigit.ohsome.oshdb.util.function.SerializableSupplier;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;


// TODO: Remove

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

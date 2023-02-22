package org.heigit.ohsome.ohsomeapi.inputprocessing;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.heigit.ohsome.oshdb.OSHDBRole;
import org.heigit.ohsome.oshdb.OSHDBTag;
import org.heigit.ohsome.oshdb.util.OSHDBTagKey;
import org.heigit.ohsome.oshdb.util.tagtranslator.OSMRole;
import org.heigit.ohsome.oshdb.util.tagtranslator.OSMTag;
import org.heigit.ohsome.oshdb.util.tagtranslator.OSMTagKey;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;

public class DummyTagTranslator implements TagTranslator {

  @Override
  public Optional<OSHDBTagKey> getOSHDBTagKeyOf(OSMTagKey osmTagKey) {
    return Optional.empty();
  }

  @Override
  public Optional<OSHDBTag> getOSHDBTagOf(OSMTag osmTag) {
    return Optional.empty();
  }

  @Override
  public Map<OSMTag, OSHDBTag> getOSHDBTagOf(Collection<OSMTag> collection) {
    return null;
  }

  @Override
  public Optional<OSHDBRole> getOSHDBRoleOf(OSMRole osmRole) {
    return Optional.empty();
  }

  @Override
  public Map<OSMRole, OSHDBRole> getOSHDBRoleOf(Collection<OSMRole> collection) {
    return null;
  }

  @Override
  public OSMTag lookupTag(OSHDBTag oshdbTag) {
    return null;
  }

  @Override
  public Map<OSHDBTag, OSMTag> lookupTag(Set<? extends OSHDBTag> set) {
    return null;
  }

  @Override
  public OSMRole lookupRole(OSHDBRole oshdbRole) {
    return null;
  }
}

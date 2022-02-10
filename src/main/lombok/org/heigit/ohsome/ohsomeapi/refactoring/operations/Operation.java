package org.heigit.ohsome.ohsomeapi.refactoring.operations;

import java.util.List;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.Result;
import org.springframework.stereotype.Component;

@Component
public interface Operation <T> {

  Object compute() throws Exception;

  Response getResponse(List<? extends Result> resultSet);

  String getDescription();

  String getUnit();

  String getMetadataDescription();

  InputProcessor getInputProcessor();
}

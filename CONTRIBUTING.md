# Code Style

We're using the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) for the source code. With the [exception](https://google.github.io/styleguide/javaguide.html#s5.3-camel-case) that the abbreviations `OSM`, `OSH` and `OSHDB` are allowed to be used in method and class names. For some popular IDEs and code linting tools you can find configuration files of the used code style in the OSHDB repository: [config/ide](https://github.com/GIScience/oshdb/tree/main/config/ide).


# Check Examples

To ensure that the ohsome API runs with a defined set of examples, we collect several examples, in addition to the integrated [unit and API tests](/src/test/java/org/heigit/ohsome/ohsomeapi). These examples are used to test the ohsome API before releases or productive deployments. If you fix a bug or implement a new feature, please think of a few exemplary requests to be added into the [check-ohsome-api repository](https://gitlab.gistools.geog.uni-heidelberg.de/giscience/big-data/ohsome/helpers/check-ohsome-api/-/issues/new). They can be added as [issue](https://gitlab.gistools.geog.uni-heidelberg.de/giscience/big-data/ohsome/helpers/check-ohsome-api/-/issues/new) or directly as [merge request](https://gitlab.gistools.geog.uni-heidelberg.de/giscience/big-data/ohsome/helpers/check-ohsome-api/-/merge_requests/new). More information, see [check-ohsome-api README](https://gitlab.gistools.geog.uni-heidelberg.de/giscience/big-data/ohsome/helpers/check-ohsome-api/-/blob/master/README.md#add-example).

package tc.oc.pgm.command.parsers;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.ParserParameters;
import tc.oc.pgm.api.filter.Filter;
import tc.oc.pgm.filters.FilterMatchModule;

public final class FilterArgumentParser
    extends MatchObjectParser<Filter, Map.Entry<String, Filter>, FilterMatchModule> {

  public FilterArgumentParser(CommandManager<CommandSender> manager, ParserParameters options) {
    super(manager, options, Filter.class, FilterMatchModule.class, "filters");
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Collection<Map.Entry<String, Filter>> objects(FilterMatchModule module) {
    return StreamSupport.stream(module.getFilterContext().spliterator(), false)
        .filter(entry -> entry != null && entry.getValue() instanceof Filter)
        .map(entry -> (Map.Entry<String, Filter>) entry)
        .collect(Collectors.toList());
  }

  @Override
  protected String getName(Map.Entry<String, Filter> obj) {
    return obj.getKey();
  }

  @Override
  protected Filter getValue(Map.Entry<String, Filter> obj) {
    return obj.getValue();
  }
}

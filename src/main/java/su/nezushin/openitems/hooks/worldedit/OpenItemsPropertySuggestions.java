package su.nezushin.openitems.hooks.worldedit;

import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Completes {@code oi:} host properties in the same shape as WorldEdit block states:
 * {@code oi:hand[facing=south}, then {@code oi:hand[facing=south] } or {@code oi:hand[facing=south,}.
 */
final class OpenItemsPropertySuggestions {

    private OpenItemsPropertySuggestions() {
    }

    static Stream<String> suggest(String patternPrefix, BlockType type, String properties) {
        if (!properties.toLowerCase(Locale.ROOT).equals(properties))
            return Stream.empty();

        var propertyMap = type.getPropertyMap();
        Set<String> matched = new HashSet<>();
        String[] parts = properties.split(",", -1);
        for (int i = 0; i < parts.length; i++) {
            String[] propVal = parts[i].split("=", -1);
            String matchProp = propVal[0].toLowerCase(Locale.ROOT);
            if (i != parts.length - 1) {
                if (propVal.length != 2)
                    return Stream.empty();
                var property = propertyMap.get(matchProp);
                if (property == null)
                    return Stream.empty();
                try {
                    property.getValueFor(propVal[1]);
                    matched.add(property.getName());
                } catch (IllegalArgumentException ignored) {
                    return Stream.empty();
                }
                continue;
            }

            String previous = Arrays.stream(parts, 0, parts.length - 1).collect(Collectors.joining(","));
            if (!previous.isEmpty())
                previous = previous + ",";
            String lastValid = patternPrefix + "[" + previous;

            if (propVal.length == 1) {
                List<? extends Property<?>> matching = propertyMap.entrySet().stream()
                        .filter(entry -> !matched.contains(entry.getKey()) && entry.getKey().startsWith(matchProp))
                        .map(entry -> (Property<?>) entry.getValue())
                        .toList();
                return switch (matching.size()) {
                    case 0 -> propertyMap.keySet().stream()
                            .filter(name -> !matched.contains(name))
                            .map(name -> lastValid + name + "=");
                    case 1 -> suggestPropertyValues(lastValid, matching.get(0), "");
                    default -> matching.stream().map(property -> lastValid + property.getName() + "=");
                };
            }

            var property = propertyMap.get(matchProp);
            if (property == null) {
                return propertyMap.keySet().stream()
                        .filter(name -> !matched.contains(name))
                        .map(name -> lastValid + name + "=");
            }
            return suggestPropertyValues(lastValid, property, propVal[1].toLowerCase(Locale.ROOT));
        }
        return Stream.empty();
    }

    private static Stream<String> suggestPropertyValues(String lastValid, Property<?> property, String typedValue) {
        List<String> values = property.getValues().stream()
                .map(value -> value.toString().toLowerCase(Locale.ROOT))
                .toList();
        List<String> matching = values.stream().filter(value -> value.startsWith(typedValue)).toList();
        if (matching.isEmpty())
            return values.stream().map(value -> lastValid + property.getName() + "=" + value);
        if (matching.size() == 1 && matching.get(0).equals(typedValue)) {
            String current = lastValid + property.getName() + "=" + typedValue;
            return Stream.of(current + "] ", current + ",");
        }
        return matching.stream().map(value -> lastValid + property.getName() + "=" + value);
    }
}

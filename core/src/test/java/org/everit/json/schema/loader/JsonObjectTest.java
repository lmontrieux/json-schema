package org.everit.json.schema.loader;

import org.everit.json.schema.SchemaException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author erosb
 */
public class JsonObjectTest {

    private Map<String, Object> storage() {
        Map<String, Object> rval = new HashMap<>();
        rval.put("a", true);
        rval.put("b", new JSONObject());
        return rval;
    }

    private final LoadingState emptyLs = JsonValueTest.emptyLs;

    @Rule
    public ExpectedException expExc = ExpectedException.none();

    @Test
    public void testHasKey() {
        assertTrue(subject().containsKey("a"));
    }

    private JsonObject subject() {
        return new JsonObject(storage(), JsonValueTest.emptyLs);
    }

    @SuppressWarnings("unchecked")
    private Consumer<JsonValue> mockConsumer() {
        return (Consumer<JsonValue>) mock(Consumer.class);
    }

    @Test
    public void testRequireWithConsumer() {
        Consumer<JsonValue> consumer = mockConsumer();
        subject().require("a", consumer);
        LoadingState lsForPath = emptyLs.childFor("a");
        verify(consumer).accept(JsonValue.of(true, lsForPath));
    }

    @Test
    public void testRequireWithConsumerFailure() {
        expExc.expect(SchemaException.class);
        expExc.expectMessage("#: required key [aaa] not found");
        Consumer<JsonValue> consumer = mockConsumer();
        subject().require("aaa", consumer);
        verify(consumer, never()).accept(any());
    }

    @Test
    public void testRequireWithFunction() {
        Function<JsonValue, Boolean> fn = val -> false;
        assertFalse(subject().require("a", fn));
    }

    @Test
    public void testRequireWithFunctionFailure() {
        expExc.expect(SchemaException.class);
        expExc.expectMessage("#: required key [aaa] not found");
        subject().require("aaa", val -> false);
    }

    @Test
    public void testMaybeWithConsumer() {
        Consumer<JsonValue> consumer = mockConsumer();
        subject().maybe("a", consumer);
        LoadingState lsForPath = emptyLs.childFor("a");
        verify(consumer).accept(JsonValue.of(true, lsForPath));
    }

    @Test
    public void testMaybeWithConsumerMiss() {
        Consumer<JsonValue> consumer = mockConsumer();
        subject().maybe("aaa", consumer);
        verify(consumer, never()).accept(any());
    }

    @Test
    public void testForEach() {
        JsonObjectIterator iterator = mock(JsonObjectIterator.class);
        subject().forEach(iterator);
        LoadingState aChild = emptyLs.childFor("a"), bChild = emptyLs.childFor("b");
        verify(iterator).apply("a", JsonValue.of(true, aChild));
        verify(iterator).apply("b", JsonValue.of(new JSONObject(), bChild));
    }

    @Test
    public void testMaybeWithFn() {
        assertEquals(Integer.valueOf(42), subject().maybe("a", obj -> 42).get());
    }

    @Test
    public void testMaybeWithFnMiss() {
        assertEquals(Optional.empty(), subject().maybe("aaaa", ls -> 42));
    }
}
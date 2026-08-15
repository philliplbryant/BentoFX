package software.coley.bentofx.persistence.impl.codec.json.mixins;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockingLayoutDto;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import static com.fasterxml.jackson.annotation.JsonInclude.Value.construct;
import static javafx.geometry.Orientation.HORIZONTAL;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.geometry.Side.TOP;
import static javafx.stage.Modality.NONE;
import static org.assertj.core.api.Assertions.assertThat;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.*;
import static software.coley.bentofx.persistence.impl.codec.json.mixins.ObjectMapperMixins.registerAll;
import static software.coley.bentofx.persistence.testfixtures.codec.dto.SampleDockingLayoutDtoFactory.*;

class ObjectMapperMixinsCompatibilityTest {

	private static final String FIELD_ALWAYS_ON_TOP = "alwaysOnTop";
	private static final String FIELD_AUTO_CLOSE_WHEN_EMPTY = "autoCloseWhenEmpty";
	private static final String FIELD_CHILDREN = "children";
	private static final String FIELD_FOCUSED = "focused";
	private static final String FIELD_FULL_SCREEN = "fullScreen";
	private static final String FIELD_HEIGHT = "height";
	private static final String FIELD_IDENTIFIER = "identifier";
	private static final String FIELD_ICONIFIED = "iconified";
	private static final String FIELD_INDEX = "index";
	private static final String FIELD_IS_CAN_SPLIT = "isCanSplit";
	private static final String FIELD_IS_COLLAPSED = "isCollapsed";
	private static final String FIELD_IS_RESIZABLE_WITH_PARENT = "isResizableWithParent";
	private static final String FIELD_MAXIMIZED = "maximized";
	private static final String FIELD_METADATA = "metadata";
	private static final String FIELD_MODALITY = "modality";
	private static final String FIELD_OPACITY = "opacity";
	private static final String FIELD_ORIENTATION = "orientation";
	private static final String FIELD_POSITION = "position";
	private static final String FIELD_PRUNE_WHEN_EMPTY = "pruneWhenEmpty";
	private static final String FIELD_RESIZABLE = "resizable";
	private static final String FIELD_SCHEMA_VERSION = "schemaVersion";
	private static final String FIELD_SELECTED_DOCKABLE_IDENTIFIER = "selectedDockableIdentifier";
	private static final String FIELD_SHOWING = "showing";
	private static final String FIELD_SIDE = "side";
	private static final String FIELD_TITLE = "title";
	private static final String FIELD_TYPE = "type";
	private static final String FIELD_UNCOLLAPSED_SIZE_PX = "uncollapsedSizePx";
	private static final String FIELD_WIDTH = "width";
	private static final String FIELD_X = "x";
	private static final String FIELD_Y = "y";


	@Test
	void everyMixinFieldNamesADtoField() {
		final List<String> inertFields = new ArrayList<>();

		ObjectMapperMixins.MIXINS_BY_DTO.forEach((dto, mixin) -> {
			final Set<String> dtoFieldNames = fieldNamesOf(dto);

			for (final Field mixinField : mixin.getDeclaredFields()) {
				if (!mixinField.isSynthetic()
						&& !dtoFieldNames.contains(mixinField.getName())) {
					inertFields.add(
							mixin.getSimpleName() + '.' + mixinField.getName()
									+ " (no such field on "
									+ dto.getSimpleName() + ')'
					);
				}
			}
		});

		assertThat(inertFields)
				.describedAs("mix-in fields matching no DTO field")
				.isEmpty();
	}

	/**
	 * {@return every field name declared by {@code type} or inherited from a
	 * superclass.}
	 *
	 * @param type the class whose field names are wanted.
	 */
	private static Set<String> fieldNamesOf(final Class<?> type) {
		final Set<String> fieldNames = new HashSet<>();

		for (Class<?> current = type;
			 current != null && current != Object.class;
			 current = current.getSuperclass()) {

			for (final Field field : current.getDeclaredFields()) {
				fieldNames.add(field.getName());
			}
		}

		return fieldNames;
	}

	@Test
	void serializesDockingLayoutUsingCommonMapperFieldNames() throws Exception {
		final DockingLayoutDto dto = createDockingLayoutDto();

		final JsonMapper codecMapper = newCodecMapper();
		final JsonMapper treeMapper = newTreeMapper();

		final JsonNode expected = normalizeJson(createExpectedDockingLayoutJson());
		final JsonNode actual = normalizeJson(
				treeMapper.readTree(codecMapper.writeValueAsBytes(dto))
		);

		assertThat(expected)
				.describedAs("expected serialized docking layout JSON")
				.isNotNull();
		assertThat(actual)
				.describedAs("actual serialized docking layout JSON")
				.isNotNull()
				.isEqualTo(expected);
	}

	@Test
	void deserializesDockingLayoutUsingMixins() throws Exception {
		final JsonMapper codecMapper = newCodecMapper();
		final JsonMapper treeMapper = newTreeMapper();

		final JsonNode expected = normalizeJson(createExpectedDockingLayoutJson());

		final DockingLayoutDto restored = codecMapper.readValue(
				treeMapper.writeValueAsBytes(expected),
				DockingLayoutDto.class
		);

		final JsonNode actual = normalizeJson(
				treeMapper.readTree(codecMapper.writeValueAsBytes(restored))
		);

		assertThat(restored)
				.describedAs("deserialized docking layout DTO")
				.isNotNull();
		assertThat(actual)
				.describedAs("reserialized docking layout JSON")
				.isEqualTo(expected);
	}

	private static JsonMapper newCodecMapper() {
		final JsonMapper mapper = JsonMapper.builder()
				.defaultPropertyInclusion(construct(ALWAYS, ALWAYS))
				.enable(SerializationFeature.WRAP_ROOT_VALUE)
				.enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
				.build();

		registerAll(mapper);
		return mapper;
	}

	private static JsonMapper newTreeMapper() {
		return JsonMapper.builder()
				.defaultPropertyInclusion(construct(ALWAYS, ALWAYS))
				.build();
	}

	private static JsonNode normalizeJson(final JsonNode node) {
		return removeNullFields(node.deepCopy());
	}

	private static JsonNode removeNullFields(final JsonNode node) {
		if (node == null || node.isNull()) {
			return node;
		}

		if (node.isObject()) {
			final ObjectNode objectNode = (ObjectNode) node;
			final List<String> fieldNames = new ArrayList<>();
			objectNode.fieldNames().forEachRemaining(fieldNames::add);

			for (final String fieldName : fieldNames) {
				final JsonNode child = objectNode.get(fieldName);
				if (child == null || child.isNull()) {
					objectNode.remove(fieldName);
				} else {
					removeNullFields(child);
				}
			}
			return objectNode;
		}

		if (node.isArray()) {
			final ArrayNode arrayNode = (ArrayNode) node;
			for (int i = 0; i < arrayNode.size(); i++) {
				final JsonNode child = arrayNode.get(i);
				if (child != null && !child.isNull()) {
					removeNullFields(child);
				}
			}
			return arrayNode;
		}

		return node;
	}

	private static JsonNode createExpectedDockingLayoutJson() {
		final JsonNodeFactory factory = JsonNodeFactory.instance;

		final ObjectNode divider = factory.objectNode();
		divider.set(FIELD_INDEX, factory.numberNode(0));
		divider.set(FIELD_POSITION, factory.numberNode(0.42));

		final ArrayNode dividerPositions = factory.arrayNode();
		dividerPositions.add(divider);

		final ObjectNode leaf = createLeafNode(factory);

		final ArrayNode children = factory.arrayNode();
		children.add(leaf.deepCopy());

		final ObjectNode branch = factory.objectNode();
		branch.set(FIELD_TYPE, factory.textNode(BRANCH_ELEMENT_NAME));
		branch.set(FIELD_IDENTIFIER, factory.textNode(BRANCH_IDENTIFIER));
		branch.set(FIELD_PRUNE_WHEN_EMPTY, factory.booleanNode(false));
		branch.set(FIELD_ORIENTATION, factory.textNode(HORIZONTAL.name()));
		branch.set(DIVIDER_POSITION_LIST_ELEMENT_NAME, dividerPositions.deepCopy());
		branch.set(FIELD_CHILDREN, children);

		final ArrayNode rootChildren = factory.arrayNode();
		rootChildren.add(branch);
		rootChildren.add(leaf.deepCopy());

		final ObjectNode rootBranch = factory.objectNode();
		rootBranch.set(FIELD_IDENTIFIER, factory.textNode(ROOT_IDENTIFIER));
		rootBranch.set(FIELD_PRUNE_WHEN_EMPTY, factory.booleanNode(false));
		rootBranch.set(FIELD_ORIENTATION, factory.textNode(VERTICAL.name()));
		rootBranch.set(DIVIDER_POSITION_LIST_ELEMENT_NAME, dividerPositions.deepCopy());
		rootBranch.set(FIELD_CHILDREN, rootChildren);

		final ObjectNode dragDropStage = factory.objectNode();
		dragDropStage.set(FIELD_TITLE, factory.textNode(STAGE_TITLE));
		dragDropStage.set(FIELD_X, factory.numberNode(10.0));
		dragDropStage.set(FIELD_Y, factory.numberNode(20.0));
		dragDropStage.set(FIELD_WIDTH, factory.numberNode(800.0));
		dragDropStage.set(FIELD_HEIGHT, factory.numberNode(600.0));
		dragDropStage.set(FIELD_MODALITY, factory.textNode(NONE.name()));
		dragDropStage.set(FIELD_OPACITY, factory.numberNode(0.9));
		dragDropStage.set(FIELD_ICONIFIED, factory.booleanNode(false));
		dragDropStage.set(FIELD_FULL_SCREEN, factory.booleanNode(false));
		dragDropStage.set(FIELD_MAXIMIZED, factory.booleanNode(true));
		dragDropStage.set(FIELD_ALWAYS_ON_TOP, factory.booleanNode(false));
		dragDropStage.set(FIELD_RESIZABLE, factory.booleanNode(true));
		dragDropStage.set(FIELD_SHOWING, factory.booleanNode(true));
		dragDropStage.set(FIELD_FOCUSED, factory.booleanNode(true));
		dragDropStage.set(FIELD_AUTO_CLOSE_WHEN_EMPTY, factory.booleanNode(true));
		dragDropStage.set(ROOT_BRANCH_ELEMENT_NAME, rootBranch.deepCopy());

		final ArrayNode rootBranches = factory.arrayNode();
		rootBranches.add(rootBranch.deepCopy());

		final ArrayNode dragDropStages = factory.arrayNode();
		dragDropStages.add(dragDropStage);

		final ObjectNode bento = factory.objectNode();
		bento.set(FIELD_IDENTIFIER, factory.textNode(BENTO_IDENTIFIER));
		bento.set(ROOT_BRANCH_LIST_ELEMENT_NAME, rootBranches);
		bento.set(DRAG_DROP_STAGE_LIST_ELEMENT_NAME, dragDropStages);

		final ArrayNode bentos = factory.arrayNode();
		bentos.add(bento);

		final ObjectNode metadata = factory.objectNode();
		metadata.set(
				FIELD_SCHEMA_VERSION,
				factory.numberNode(DockingLayoutDto.getCurrentSchemaVersion())
		);

		final ObjectNode dockingLayout = factory.objectNode();
		dockingLayout.set(FIELD_METADATA, metadata);
		dockingLayout.set(BENTO_LIST_ELEMENT_NAME, bentos);

		final ObjectNode wrapped = factory.objectNode();
		wrapped.set(DOCKING_LAYOUT_ROOT_ELEMENT_NAME, dockingLayout);

		return wrapped;
	}

	private static ObjectNode createLeafNode(final JsonNodeFactory factory) {
		final ObjectNode leaf = factory.objectNode();
		leaf.set(FIELD_TYPE, factory.textNode(LEAF_ELEMENT_NAME));
		leaf.set(FIELD_IDENTIFIER, factory.textNode(LEAF_IDENTIFIER));
		leaf.set(FIELD_PRUNE_WHEN_EMPTY, factory.booleanNode(true));
		leaf.set(FIELD_SELECTED_DOCKABLE_IDENTIFIER, factory.textNode(DOCKABLE_IDENTIFIER));
		leaf.set(FIELD_SIDE, factory.textNode(TOP.name()));
		leaf.set(FIELD_IS_RESIZABLE_WITH_PARENT, factory.booleanNode(true));
		leaf.set(FIELD_IS_CAN_SPLIT, factory.booleanNode(true));
		leaf.set(FIELD_UNCOLLAPSED_SIZE_PX, factory.numberNode(321.0));
		leaf.set(FIELD_IS_COLLAPSED, factory.booleanNode(false));

		final ObjectNode dockable = factory.objectNode();
		dockable.set(FIELD_IDENTIFIER, factory.textNode(DOCKABLE_IDENTIFIER));

		final ArrayNode dockables = factory.arrayNode();
		dockables.add(dockable);
		leaf.set(DOCKABLE_LIST_ELEMENT_NAME, dockables);

		return leaf;
	}
}

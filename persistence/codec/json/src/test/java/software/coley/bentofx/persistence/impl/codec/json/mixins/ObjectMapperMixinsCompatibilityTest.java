package software.coley.bentofx.persistence.impl.codec.json.mixins;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.BentoStateDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DividerPositionDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerBranchDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerLeafDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerRootBranchDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockableDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockingLayoutDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DragDropStageDto;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import static com.fasterxml.jackson.annotation.JsonInclude.Value.construct;
import static javafx.geometry.Orientation.HORIZONTAL;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.geometry.Side.TOP;
import static javafx.stage.Modality.NONE;
import static org.assertj.core.api.Assertions.assertThat;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.*;
import static software.coley.bentofx.persistence.impl.codec.json.mixins.ObjectMapperMixins.registerAll;

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
	private static final String FIELD_MODALITY = "modality";
	private static final String FIELD_OPACITY = "opacity";
	private static final String FIELD_ORIENTATION = "orientation";
	private static final String FIELD_POSITION = "position";
	private static final String FIELD_PRUNE_WHEN_EMPTY = "pruneWhenEmpty";
	private static final String FIELD_RESIZABLE = "resizable";
	private static final String FIELD_SELECTED_DOCKABLE_IDENTIFIER = "selectedDockableIdentifier";
	private static final String FIELD_SHOWING = "showing";
	private static final String FIELD_SIDE = "side";
	private static final String FIELD_TITLE = "title";
	private static final String FIELD_TYPE = "type";
	private static final String FIELD_UNCOLLAPSED_SIZE_PX = "uncollapsedSizePx";
	private static final String FIELD_WIDTH = "width";
	private static final String FIELD_X = "x";
	private static final String FIELD_Y = "y";

	private static final String BENTO_IDENTIFIER = "bento-1";
	private static final String BRANCH_IDENTIFIER = "branch-1";
	private static final String DOCKABLE_IDENTIFIER = "dockable-1";
	private static final String LEAF_IDENTIFIER = "leaf-1";
	private static final String ROOT_IDENTIFIER = "root-1";
	private static final String STAGE_TITLE = "Stage";

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
				.isNotNull();
		assertThat(actual)
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

		assertThat(restored).isNotNull();
		assertThat(actual).isEqualTo(expected);
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

	private static DockingLayoutDto createDockingLayoutDto() {
		final DockableDto dockable = new DockableDto();
		dockable.identifier = DOCKABLE_IDENTIFIER;

		final DockContainerLeafDto leaf = new DockContainerLeafDto();
		leaf.identifier = LEAF_IDENTIFIER;
		leaf.pruneWhenEmpty = true;
		leaf.selectedDockableIdentifier = DOCKABLE_IDENTIFIER;
		leaf.side = TOP;
		leaf.isResizableWithParent = true;
		leaf.isCanSplit = true;
		leaf.uncollapsedSizePx = 321.0;
		leaf.isCollapsed = false;
		leaf.dockables.add(dockable);

		final DividerPositionDto divider = new DividerPositionDto();
		divider.index = 0;
		divider.position = 0.42;

		final DockContainerBranchDto branch = new DockContainerBranchDto();
		branch.identifier = BRANCH_IDENTIFIER;
		branch.pruneWhenEmpty = false;
		branch.orientation = HORIZONTAL;
		branch.dividerPositions.add(divider);
		branch.children.add(leaf);

		final DockContainerRootBranchDto root = new DockContainerRootBranchDto();
		root.identifier = ROOT_IDENTIFIER;
		root.pruneWhenEmpty = false;
		root.orientation = VERTICAL;
		root.dividerPositions.add(divider);
		root.branches.add(branch);
		root.leaf = leaf;

		final DragDropStageDto stage = new DragDropStageDto();
		stage.title = STAGE_TITLE;
		stage.x = 10.0;
		stage.y = 20.0;
		stage.width = 800.0;
		stage.height = 600.0;
		stage.modality = NONE;
		stage.opacity = 0.9;
		stage.iconified = false;
		stage.fullScreen = false;
		stage.maximized = true;
		stage.alwaysOnTop = false;
		stage.resizable = true;
		stage.showing = true;
		stage.focused = true;
		stage.autoCloseWhenEmpty = true;
		stage.dockContainerRootBranchDto = root;

		final BentoStateDto bento = new BentoStateDto();
		bento.identifier = BENTO_IDENTIFIER;
		bento.rootBranches.add(root);
		bento.dragDropStages.add(stage);

		final DockingLayoutDto layout = new DockingLayoutDto();
		layout.bentoStates.add(bento);

		return layout;
	}

	private static JsonNode createExpectedDockingLayoutJson() {
		final JsonNodeFactory factory = JsonNodeFactory.instance;

		final ObjectNode divider = factory.objectNode();
		divider.put(FIELD_INDEX, 0);
		divider.put(FIELD_POSITION, 0.42);

		final ArrayNode dividerPositions = factory.arrayNode();
		dividerPositions.add(divider);

		final ObjectNode leaf = createLeafNode(factory);

		final ArrayNode children = factory.arrayNode();
		children.add(leaf.deepCopy());

		final ObjectNode branch = factory.objectNode();
		branch.set(FIELD_TYPE, factory.textNode(BRANCH_ELEMENT_NAME));
		branch.set(FIELD_IDENTIFIER, factory.textNode(BRANCH_IDENTIFIER));
		branch.put(FIELD_PRUNE_WHEN_EMPTY, false);
		branch.set(FIELD_ORIENTATION, factory.textNode(HORIZONTAL.name()));
		branch.set(DIVIDER_POSITION_LIST_ELEMENT_NAME, dividerPositions.deepCopy());
		branch.set(FIELD_CHILDREN, children);

		final ArrayNode branches = factory.arrayNode();
		branches.add(branch);

		final ObjectNode rootBranch = factory.objectNode();
		rootBranch.set(FIELD_IDENTIFIER, factory.textNode(ROOT_IDENTIFIER));
		rootBranch.put(FIELD_PRUNE_WHEN_EMPTY, false);
		rootBranch.set(FIELD_ORIENTATION, factory.textNode(VERTICAL.name()));
		rootBranch.set(DIVIDER_POSITION_LIST_ELEMENT_NAME, dividerPositions.deepCopy());
		rootBranch.set(BRANCH_LIST_ELEMENT_NAME, branches);
		rootBranch.set(LEAF_ELEMENT_NAME, leaf.deepCopy());

		final ObjectNode dragDropStage = factory.objectNode();
		dragDropStage.set(FIELD_TITLE, factory.textNode(STAGE_TITLE));
		dragDropStage.put(FIELD_X, 10.0);
		dragDropStage.put(FIELD_Y, 20.0);
		dragDropStage.put(FIELD_WIDTH, 800.0);
		dragDropStage.put(FIELD_HEIGHT, 600.0);
		dragDropStage.put(FIELD_MODALITY, NONE.name());
		dragDropStage.put(FIELD_OPACITY, 0.9);
		dragDropStage.put(FIELD_ICONIFIED, false);
		dragDropStage.put(FIELD_FULL_SCREEN, false);
		dragDropStage.put(FIELD_MAXIMIZED, true);
		dragDropStage.put(FIELD_ALWAYS_ON_TOP, false);
		dragDropStage.put(FIELD_RESIZABLE, true);
		dragDropStage.put(FIELD_SHOWING, true);
		dragDropStage.put(FIELD_FOCUSED, true);
		dragDropStage.put(FIELD_AUTO_CLOSE_WHEN_EMPTY, true);
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

		final ObjectNode dockingLayout = factory.objectNode();
		dockingLayout.set(BENTO_LIST_ELEMENT_NAME, bentos);

		final ObjectNode wrapped = factory.objectNode();
		wrapped.set(DOCKING_LAYOUT_ROOT_ELEMENT_NAME, dockingLayout);

		return wrapped;
	}

	private static ObjectNode createLeafNode(final JsonNodeFactory factory) {
		final ObjectNode leaf = factory.objectNode();
		leaf.set(FIELD_TYPE, factory.textNode(LEAF_ELEMENT_NAME));
		leaf.set(FIELD_IDENTIFIER, factory.textNode(LEAF_IDENTIFIER));
		leaf.put(FIELD_PRUNE_WHEN_EMPTY, true);
		leaf.set(FIELD_SELECTED_DOCKABLE_IDENTIFIER, factory.textNode(DOCKABLE_IDENTIFIER));
		leaf.set(FIELD_SIDE, factory.textNode(TOP.name()));
		leaf.put(FIELD_IS_RESIZABLE_WITH_PARENT, true);
		leaf.put(FIELD_IS_CAN_SPLIT, true);
		leaf.put(FIELD_UNCOLLAPSED_SIZE_PX, 321.0);
		leaf.put(FIELD_IS_COLLAPSED, false);

		final ObjectNode dockable = factory.objectNode();
		dockable.set(FIELD_IDENTIFIER, factory.textNode(DOCKABLE_IDENTIFIER));

		final ArrayNode dockables = factory.arrayNode();
		dockables.add(dockable);
		leaf.set(DOCKABLE_LIST_ELEMENT_NAME, dockables);

		return leaf;
	}
}

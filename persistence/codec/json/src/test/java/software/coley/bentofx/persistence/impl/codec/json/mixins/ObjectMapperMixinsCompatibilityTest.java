package software.coley.bentofx.persistence.impl.codec.json.mixins;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.stage.Modality;
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

import static com.fasterxml.jackson.annotation.JsonInclude.Value.ALL_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.*;
import static software.coley.bentofx.persistence.impl.codec.json.mixins.ObjectMapperMixins.registerAll;

class ObjectMapperMixinsCompatibilityTest {

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
				.defaultPropertyInclusion(ALL_ALWAYS)
				.enable(SerializationFeature.WRAP_ROOT_VALUE)
				.enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
				.build();

		registerAll(mapper);
		return mapper;
	}

	private static JsonMapper newTreeMapper() {
		return JsonMapper.builder()
				.defaultPropertyInclusion(ALL_ALWAYS)
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
		dockable.identifier = "dockable-1";

		final DockContainerLeafDto leaf = new DockContainerLeafDto();
		leaf.identifier = "leaf-1";
		leaf.pruneWhenEmpty = true;
		leaf.selectedDockableIdentifier = "dockable-1";
		leaf.side = Side.TOP;
		leaf.isResizableWithParent = true;
		leaf.isCanSplit = true;
		leaf.uncollapsedSizePx = 321.0;
		leaf.isCollapsed = false;
		leaf.dockables.add(dockable);

		final DividerPositionDto divider = new DividerPositionDto();
		divider.index = 0;
		divider.position = 0.42;

		final DockContainerBranchDto branch = new DockContainerBranchDto();
		branch.identifier = "branch-1";
		branch.pruneWhenEmpty = false;
		branch.orientation = Orientation.HORIZONTAL;
		branch.dividerPositions.add(divider);
		branch.children.add(leaf);

		final DockContainerRootBranchDto root = new DockContainerRootBranchDto();
		root.identifier = "root-1";
		root.pruneWhenEmpty = false;
		root.orientation = Orientation.VERTICAL;
		root.dividerPositions.add(divider);
		root.branches.add(branch);
		root.leaf = leaf;

		final DragDropStageDto stage = new DragDropStageDto();
		stage.title = "Stage";
		stage.x = 10.0;
		stage.y = 20.0;
		stage.width = 800.0;
		stage.height = 600.0;
		stage.modality = Modality.NONE;
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
		bento.identifier = "bento-1";
		bento.rootBranches.add(root);
		bento.dragDropStages.add(stage);

		final DockingLayoutDto layout = new DockingLayoutDto();
		layout.bentoStates.add(bento);

		return layout;
	}

	private static JsonNode createExpectedDockingLayoutJson() {
		final JsonNodeFactory factory = JsonNodeFactory.instance;

		final ObjectNode divider = factory.objectNode();
		divider.put("index", 0);
		divider.put("position", 0.42);

		final ArrayNode dividerPositions = factory.arrayNode();
		dividerPositions.add(divider);

		final ObjectNode leaf = createLeafNode(factory);

		final ArrayNode children = factory.arrayNode();
		children.add(leaf.deepCopy());

		final ObjectNode branch = factory.objectNode();
		branch.put("type", BRANCH_ELEMENT_NAME);
		branch.put("identifier", "branch-1");
		branch.put("pruneWhenEmpty", false);
		branch.put("orientation", Orientation.HORIZONTAL.name());
		branch.set(DIVIDER_POSITION_LIST_ELEMENT_NAME, dividerPositions.deepCopy());
		branch.set("children", children);

		final ArrayNode branches = factory.arrayNode();
		branches.add(branch);

		final ObjectNode rootBranch = factory.objectNode();
		rootBranch.put("identifier", "root-1");
		rootBranch.put("pruneWhenEmpty", false);
		rootBranch.put("orientation", Orientation.VERTICAL.name());
		rootBranch.set(DIVIDER_POSITION_LIST_ELEMENT_NAME, dividerPositions.deepCopy());
		rootBranch.set(BRANCH_LIST_ELEMENT_NAME, branches);
		rootBranch.set(LEAF_ELEMENT_NAME, leaf.deepCopy());

		final ObjectNode dragDropStage = factory.objectNode();
		dragDropStage.put("title", "Stage");
		dragDropStage.put("x", 10.0);
		dragDropStage.put("y", 20.0);
		dragDropStage.put("width", 800.0);
		dragDropStage.put("height", 600.0);
		dragDropStage.put("modality", Modality.NONE.name());
		dragDropStage.put("opacity", 0.9);
		dragDropStage.put("iconified", false);
		dragDropStage.put("fullScreen", false);
		dragDropStage.put("maximized", true);
		dragDropStage.put("alwaysOnTop", false);
		dragDropStage.put("resizable", true);
		dragDropStage.put("showing", true);
		dragDropStage.put("focused", true);
		dragDropStage.put("autoCloseWhenEmpty", true);
		dragDropStage.set(ROOT_BRANCH_ELEMENT_NAME, rootBranch.deepCopy());

		final ArrayNode rootBranches = factory.arrayNode();
		rootBranches.add(rootBranch.deepCopy());

		final ArrayNode dragDropStages = factory.arrayNode();
		dragDropStages.add(dragDropStage);

		final ObjectNode bento = factory.objectNode();
		bento.put("identifier", "bento-1");
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
		leaf.put("type", LEAF_ELEMENT_NAME);
		leaf.put("identifier", "leaf-1");
		leaf.put("pruneWhenEmpty", true);
		leaf.put("selectedDockableIdentifier", "dockable-1");
		leaf.put("side", Side.TOP.name());
		leaf.put("isResizableWithParent", true);
		leaf.put("isCanSplit", true);
		leaf.put("uncollapsedSizePx", 321.0);
		leaf.put("isCollapsed", false);

		final ObjectNode dockable = factory.objectNode();
		dockable.put("identifier", "dockable-1");

		final ArrayNode dockables = factory.arrayNode();
		dockables.add(dockable);
		leaf.set(DOCKABLE_LIST_ELEMENT_NAME, dockables);

		return leaf;
	}
}

# Bento layout persistence diagrams

For implementation details and application integration guidance, see [Docking Layout Persistence Implementation](docking-layout-persistence.md). For a high-level overview, see the [BentoFX Persistence guide](../../README-PERSISTENCE.md).


## Components overview

```mermaid
classDiagram
  direction LR

  class LayoutSaver {
    +saveLayout()
    +close()
  }

  class LayoutRestorer {
    +doesLayoutExist()
    +restoreLayout(defaultDockingLayout)
    +close()
  }
  
  class BentoProvider {
      +getAllBentos()
      +getBento(identifier)
  }

  class LayoutStorage {
    +exists()
    +openInputStream()
    +openOutputStream()
    +close()
  }

  class LayoutPersistenceProfile {
    +layoutIdentifier()
    +codecIdentifier()
    +storageIdentifier()
  }

  class LayoutPersistenceComponentProvider {
    +getIdentifier()
    +isDefault()
  }

  class DockingLayoutPersistenceProvider {
    +getLayoutSaver(profile, bentoProvider)
    +getLayoutRestorer(profile, providers)
    +saveLayout(profile, bentoProvider)
    +getStoredLayoutIdentifiers(profile)
    +getStoredLayouts(profile)
    +isLayoutStored(profile)
    +deleteLayout(profile)
  }

  class LayoutStorageProvider {
    +getLayoutStorage(layoutId, codecId)
    +getLayoutIdentifiers(codecId)
    +isLayoutStored(layoutId, codecId)
    +deleteLayout(layoutId, codecId)
  }

  class LayoutCodec {
    +encode(layout,out)
    +decode(in) PersistableLayout
  }

  class PersistableLayout {
    +displayName()
    +bentoStates()
  }

  class BentoState {
    +getRootBranchStates()
    +getDragDropStageStates()
  }

  class DockBuilding {
    +root(identifier)
    +branch(identifier)
    +leaf(identifier)
  }

  class DockableStateProvider {
    +resolveDockableState(identifier)
  }

  class DockableState {
    +getIdentifier()
    +getDockableNode()
    +getTitle()
  }
  
  class StageIconImageProvider {
      +getStageIcons()
  }
  
  class DockContainerLeafMenuFactoryProvider {
      +getDockContainerLeafMenuFactory(identifier)
  }
  
  LayoutCodecProvider --|> LayoutPersistenceComponentProvider
  LayoutStorageProvider --|> LayoutPersistenceComponentProvider

  DockingLayoutPersistenceProvider --> LayoutSaver : creates
  DockingLayoutPersistenceProvider --> LayoutRestorer : creates
  DockingLayoutPersistenceProvider --> LayoutStorageProvider : lists, tests, deletes through
  DockingLayoutPersistenceProvider --> LayoutCodec : decodes stored layouts to read display names

  LayoutSaver --> BentoProvider : get container graph
  LayoutSaver --> BentoState : builds
  LayoutSaver --> PersistableLayout : wraps state and display name in
  PersistableLayout --> BentoState : holds
  LayoutSaver --> LayoutCodec : encodes
  LayoutSaver --> LayoutStorage : writes

  LayoutRestorer --> LayoutPersistenceProfile : uses provider identifiers
  LayoutRestorer --> Supplier~DockingLayout~ : get default layout
  LayoutRestorer --> LayoutStorage : reads
  LayoutRestorer --> LayoutCodec : decodes
  LayoutRestorer --> BentoProvider : resolves Bentos
  LayoutRestorer --> Bento : resolves DockBuilding
  LayoutRestorer --> DockBuilding : creates and restores containers
  LayoutRestorer --> DockableStateProvider : resolves dockable states
  DockableStateProvider --> DockableState : supplies
  LayoutRestorer --> DockableState : restores dockables from
  LayoutRestorer --> StageIconImageProvider: resolves icons
  LayoutRestorer --> DockContainerLeafMenuFactoryProvider: resolves DockContainerLeafMenuFactory
  LayoutRestorer --> DockContainerLeafMenuFactory: builds ContextMenu
  LayoutRestorer --> Consumer~Dockable~ : notifies Dockable constructed
```

## Persistence startup sequence

```mermaid
sequenceDiagram
    autonumber
    actor BoxApp
    participant persistenceProvider as DockingLayoutPersistenceProvider
    participant profile as LayoutPersistenceProfile
    participant serviceLoader as ServiceLoader
    participant codecProvider as LayoutCodecProvider
    participant codec as LayoutCodec
    participant storageProvider as LayoutStorageProvider
    participant storage as LayoutStorage
    participant layoutSaver as LayoutSaver
    participant layoutRestorer as LayoutRestorer
    participant supplier as Supplier<DockingLayout>
    participant dockableProvider as DockableStateProvider
    participant dockableState as DockableState
    participant consumer as Consumer<Dockable>

    BoxApp->>persistenceProvider: constructor()
    BoxApp->>profile: optional codec and storage identifiers

    Note over BoxApp,layoutRestorer: Restore first, so that the layout is applied before a saver exists
    BoxApp->>persistenceProvider:getLayoutRestorer(profile, providers)
    persistenceProvider->>serviceLoader: load(LayoutCodecProvider)
    persistenceProvider->>codecProvider: select by profile, single provider, or default
    persistenceProvider->>codecProvider: getLayoutCodec()
    codecProvider->>codec:constructor()
    persistenceProvider->>serviceLoader: load(LayoutStorageProvider)
    persistenceProvider->>storageProvider: select by profile, single provider, or default
    persistenceProvider->>storageProvider: getLayoutStorage(layoutId, codecId)
    storageProvider->>storage:constructor()
    persistenceProvider->>layoutRestorer:constructor(codec, storage, providers)

    BoxApp->>layoutRestorer:restoreLayout(defaultLayoutSupplier)
    layoutRestorer->>storage:exists()
    alt normal flow
        alt layout exists
            layoutRestorer->>storage:read()
            layoutRestorer->>codec:decode()
            layoutRestorer->>dockableProvider:resolveDockableState(identifier)
            dockableProvider-->>layoutRestorer:DockableState
            layoutRestorer->>dockableState:get runtime values
            layoutRestorer->>consumer:consume(Dockable)
        else layout does not exist
            layoutRestorer->>supplier:get()
        end
    else exception
        layoutRestorer->>supplier:get()
    end
    layoutRestorer-->>BoxApp:DockingLayout
    BoxApp->>layoutRestorer:close()
    layoutRestorer->>storage:close()
    BoxApp->>BoxApp:applyDockingLayout(DockingLayout)

    Note over BoxApp,layoutSaver: The saver is obtained last and kept, so auto-save covers the session
    BoxApp->>persistenceProvider:getLayoutSaver(profile, bentoProvider)
    persistenceProvider->>layoutSaver:constructor(codec, storage, bentoProvider)
    persistenceProvider->>layoutSaver:startAutoSave()
    layoutSaver->>layoutSaver:enableAutoSave(5, MINUTES)
    loop every interval, only when a DockEvent arrived
        layoutSaver->>layoutSaver:saveLayout()
        layoutSaver->>codec:encode()
        layoutSaver->>storage:write()
    end

    BoxApp->>BoxApp:onCloseRequest(saveDockingLayout)
    BoxApp->>layoutSaver:saveLayout()
    layoutSaver->>codec:encode()
    layoutSaver->>storage:write()
    BoxApp->>layoutSaver:close()
    layoutSaver->>layoutSaver:remove DockEventListener, stop scheduler
    layoutSaver->>storage:close()
```

## Persistence Demo: Applying a DockingLayout

A `DockingLayout` holds one `BentoLayout` per persisted `Bento`, and the application decides what to do with each. Applying
can fail: a stored layout may name a `Bento` this application does not have, or hold a number of root branches it does not
know how to place. The application reports whether anything was applied and falls back to the default layout when nothing
was, because a `Stage` that never receives a `Scene` is never shown.

```mermaid
sequenceDiagram
    autonumber
    actor BoxApp
    participant dockingLayout as DockingLayout
    participant bentoLayout as BentoLayout
    participant stage as Stage
    participant dragDropStage as DragDropStage

    BoxApp->>dockingLayout:getBentoLayouts()
    loop for each BentoLayout
        BoxApp->>bentoLayout:matchesIdentity(bento)
        alt matches this Bento
            BoxApp->>bentoLayout:getRootBranches()
            alt exactly one root branch
                BoxApp->>stage:setScene(new Scene(rootBranch))
                BoxApp->>stage:show()
                BoxApp->>bentoLayout:getDragDropStages()
                loop for each stage that was showing when saved
                    BoxApp->>dragDropStage:show()
                end
            else cannot be placed
                BoxApp->>BoxApp:report not applied
            end
        else another Bento's layout
            BoxApp->>BoxApp:report not applied
        end
    end

    alt nothing was applied
        BoxApp->>BoxApp:applyDockingLayout(getDefaultDockingLayout())
    end
```

# Bento layout persistence diagrams

For implementation details and application integration guidance, see [Docking Layout Persistence Implementation](docking-layout-persistence.md). For a high-level overview, see the [README Persistence Framework](../../README.md#persistence).


## Components overview

```mermaid
classDiagram
  direction LR

  class LayoutSaver {
    +saveLayout()
  }

  class LayoutRestorer {
    +restoreLayout(defaultDockingLayout)
  }
  
  class BentoProvider {
      +getAllBentos()
      +getBento(identifier)
  }

  class LayoutStorage {
    +openInputStream()
    +openOutputStream()
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

  class LayoutCodec {
    +encode(state,out)
    +decode(in) BentoState
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

  LayoutSaver --> BentoProvider : get container graph
  LayoutSaver --> BentoState : builds
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
    BoxApp->>profile: optional provider identifiers
    persistenceProvider->>serviceLoader: load(LayoutCodecProvider)
    persistenceProvider->>codecProvider: select by profile, single provider, or default
    persistenceProvider->>codecProvider: getLayoutCodec()
    codecProvider->>codec:constructor()
    persistenceProvider->>serviceLoader: load(LayoutStorageProvider)
    persistenceProvider->>storageProvider: select by profile, single provider, or default
    persistenceProvider->>storageProvider: getLayoutStorage()
    storageProvider->>storage:constructor()
    persistenceProvider->>layoutSaver:constructor(codec, storage, ...)
    layoutSaver->>layoutSaver:autoSave()
    layoutSaver->>layoutSaver:saveLayout()
    layoutSaver->>codec:encode()
    layoutSaver->>storage:write()
    persistenceProvider->>layoutRestorer:constructor(codec, storage, ...)

    BoxApp->>persistenceProvider:getLayoutSaver(profile)
    BoxApp->>BoxApp:onCloseRequest(LayoutSaver::saveLayout)
    BoxApp->>layoutSaver:saveLayout()
    layoutSaver->>codec:encode()
    layoutSaver->>storage:write()
    
    BoxApp->>persistenceProvider:getLayoutRestorer(profile)
    BoxApp->>layoutRestorer:restoreLayout()
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
    BoxApp->>BoxApp:applyLayout(DockingLayout)
```

## Persistence Demo: Applying a DockingLayout

```mermaid
sequenceDiagram
    autonumber
    actor BoxApp
    participant layoutRestorer as LayoutRestorer
    participant dockingLayout as DockingLayout
    participant consumer as Consumer<Dockable>
    
    BoxApp->>layoutRestorer:restoreLayout()
    activate layoutRestorer
    layoutRestorer-->>BoxApp: return DockingLayout
    BoxApp->>BoxApp:applyLayout(DockingLayout)
    
```

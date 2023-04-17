val moduleWithSimpleExport = """(module
  (func (export "simple_function") (result i32)
    i32.const 42
  )
)
"""

val moduleWithSimpleImport = """(module
  (import "env" "simple_function" (func (result i32)))
)
"""

val moduleWithMemoryImport = """(module
  (import "env" "memory" (memory 1))
)
"""

val moduleWithSimpleImportAndExport = """(module
  (import "env" "simple_function" (func (result i32)))
  (func (export "simple_function") (result i32)
    i32.const 42
  )
)
"""

val invalidWat = """(module
  (import "env" "memory" (memory 1))
  (import "env" "memory" (memory 1))
)
"""
package util

const val moduleWithSimpleExport = """(module
  (func (export "simple_function") (result i32)
    i32.const 42
  )
)
"""

const val moduleWithSimpleImport = """(module
  (import "env" "simple_function" (func (result i32)))
)
"""

const val moduleWithMemoryImport = """(module
  (import "env" "memory" (memory 1))
)
"""

const val moduleWithSimpleImportAndExport = """(module
  (import "env" "simple_function" (func (result i32)))
  (func (export "simple_function") (result i32)
    i32.const 42
  )
)
"""

const val moduleWithTwoExports = """(module
  (func (export "first"))
  (func (export "second")))
"""

const val additionModule = """(module
  (func ${'$'}add (param ${'$'}x i32) (param ${'$'}y i32) (result i32)
    local.get ${'$'}x
    local.get ${'$'}y
    i32.add)
  (export "" (func ${'$'}add))
  (export "add" (func ${'$'}add))
)
"""

const val invalidWat = """(module
  (import "env" "memory" (memory 1))
  (import "env" "memory" (memory 1))
)
"""

const val unreachableTrap = """(module
  (func (export "traps") (param i32) (result i32)
    (block (result i32)
      unreachable
    )
  )
)
"""

const val divideByZeroTrap = """(module
  (func (export "traps") (param i32) (result i32)
    local.get 0
    i32.const 0
    i32.div_s
  )
)
"""

const val memoryOutOfBoundsTrap = """(module
  (memory 1)
  (func (export "traps") (param i32) (result i32)
    i32.const 65536
    i32.load
  )
)
"""



val WAT_BINARY: ByteArray =
    """(module  (func (export "add") (param ${"$"}p1 i32) (param ${"$"}p2 i32) (result i32)    local.get ${"$"}p1
local.get ${"$"}p2
i32.add))""".encodeToByteArray()

val MEM2: ByteArray = ("(module" +
        "  (import \"mem\" \"two\" (memory \$mem2 13 37))" +
        ")").encodeToByteArray()

val IMPORT_WAT_BINARY: ByteArray = """(module  (global ${"$"}m1 (import "globals" "mutable") (mut i32))
(global ${"$"}c2 (import "globalz" "const") i64)
(func ${"$"}hello (import "first" "package"))
(import "tbl" "small" (table 0 4 anyfunc))
(import "tbl" "big" (table 12 1995 anyfunc))
(import "lua" "integration" (table 1 anyfunc))
(import "env" "memory" (memory ${"$"}mem 1))
(import "" "package" (func ${"$"}world (param ${"$"}p1 i32)))
(import "xyz" "return" (func (result i32)))
(import "xyz" "return" (func (param i32 i32 i32 i32 i32)))
(func (export "run") (call ${"$"}hello))
)""".encodeToByteArray()
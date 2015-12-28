# xand

You've heard of `and`, `or`, & `xor`. Hell, you've seen `nor`, `nand`, `xnor`, you name it!

Long have we computer scientists been taught that there are [16 distinct binary operations](https://en.wikipedia.org/wiki/Truth_table#Binary_operations). It's just combinatorics: each output has 2 possible values, each operator synthesizes 4 outputs, so each of 2<sup>4</sup> = 16 possible combinations of outputs uniquely defines an operator. Anything else would be crazy, they said...

Yet with this lesson comes an unease. That timid voice in the back of our minds that starts asking the question before we shut it up like it's a naive child. That voice that says surely...*surely* if there's an exclusive `or`, there *must* be an exclusive `and`...right?

A truth so obvious it dares us to ignore it - and we have. You won't find it in a textbook. There's no white paper, no conference. Naught from computer scientists, mathematicians, or even philosophers. But here it is at last. The computer instruction [they](https://en.wikipedia.org/wiki/Illuminati) won't tell you about.

> **xand** (ĕks′ănd′)
>
> *n.*
>
> &nbsp;&nbsp;&nbsp;&nbsp;A logical operator that returns true if exactly one of its operands are both true.

## Quick Start

1. Install [sbt](http://www.scala-sbt.org/).
2. `git clone https://github.com/ajvondrak/xand.git`
3. `cd xand`
4. `sbt run`

## How It Works

In layman's terms, `xand` is a binary operator that takes three arguments and gives you four times the power. It's literally infinitely more versatile than any ordinary computer instruction, because `xand` is an instruction from which all others can follow. [One instruction to rule them all.](https://en.wikipedia.org/wiki/One_instruction_set_computer)

Given

```
xand a b c
```

the CPU's sole job is essentially to:

1. Subtract `b` from `a` destructively (i.e., `a -= b`)
2. Check if the new value of `a` is less than or equal to zero
3. Branch to instruction `c` if indeed `a <= 0`, otherwise falling through to the next instruction

With a more rudimentary, plebeian instruction set, you might equivalently say

```
sub a a b
blez a c
```

But those are the ISAs of the past. Anything you can do with a computer, you can do with `xand`.

Some have called this instruction an awkward name like [`subleq`](http://arxiv.org/pdf/1106.2593), but that barely even makes sense. All we're really doing is an exclusive `and`, why don't we just call it what it is?

## Architecture

You've gotten the gist of how `xand` the *instruction* works, but it's important to note the implementation details about how xand the *project* works.

This project provides
- a [VM](https://en.wikipedia.org/wiki/Virtual_machine) that executes `xand` instructions (and only `xand` instructions)
- a simulator that provides a pretty way to watch your code as it runs on the xand VM
- a compiler that translates a somewhat higher-level assembly language into xand bytecode that the VM can execute

### VM

The VM is a facsimile of a very basic hardware system consisting of:
- a general-purpose memory array
- a program counter (separate from the memory) that tracks the memory address currently being executed by the CPU
- a CPU that executes `xand` instructions read from the memory

The memory has been purposefully kept small, just so that the entire contents can comfortably be displayed on one screen by the simulator. Each memory cell consists of one 8-bit byte. There are 128 total memory cells addressed 0-127:

| address | contents    |
----------|--------------
| 0       | `0000 0000` |
| 1       | `0000 0000` |
| 2       | `0000 0000` |
| 3       | `0000 0000` |
| ...     | ...         |
| 127     | `0000 0000` |

There's only one instruction, so the CPU doesn't need opcodes to distinguish different operations. To encode an `xand`, we simply place its operands in consecutive memory slots. If we put `xand a b c` at address 0, it'd look like

| address | contents    |
----------|--------------
| 0       | `a`         |
| 1       | `b`         |
| 2       | `c`         |
| 3       | `0000 0000` |
| ...     | ...         |
| 127     | `0000 0000` |

Thus we see that each operand of an `xand` is also itself limited to an 8-bit value.

So why are there 128 addresses instead of 256? Couldn't the `c` above be 8 bits long, thus telling the `xand` to branch to one of 2<sup>8</sup> = 256 addresses?

In principle, yes. But just in case we don't want our simulator to run forever, the CPU has a built-in *halting condition*. If the CPU is ever told to read a negative address, it will halt execution. To that end, the CPU interprets memory contents as *signed* bytes - numbers between -128 and +127 inclusive. (For once, the JVM's decision to treat bytes as a signed data type comes in handy!) Since negative addresses halt execution, we effectively halve our addressable space (i.e., effectively using 7 of 8 bits, since one of them indicates the sign).

Furthermore, the CPU is doing arithmetic. Just two operations (subtraction and comparison), but arithmetic all the same. It needs some concept of a negative value, since `xand` has to test whether the result of a subtraction is less than or equal to zero. Again, in principle you might cleverly detect [arithmetic underflow](https://en.wikipedia.org/wiki/Arithmetic_underflow) or whatnot and squeeze out that 8th bit. But between the halting condition, negative values providing useful semantics, and just general ease of implementation, it's simpler to go with a 7-bit address space.

Taken altogether, the CPU's basic pseudocode algorithm is

```
program_counter = 0

while (true) {
  a = program_counter
  b = program_counter + 1
  c = program_counter + 2

  halt if a < 0 or b < 0 or c < 0

  memory[a] -= memory[b]

  if memory[a] <= 0
    program_counter = c
  else
    program_counter += 3
}
```

Notice a few things:
- The program counter starts at address 0
- Subtraction is memory-to-memory, no immediate values
- The third argument, `c`, is an immediate value representing a new address
- The program counter increments by 3 because each `xand` is encoded as 3 consecutive signed bytes

The program counter is stored as a signed byte, since we use it as the value for `a` and it is assigned a value from `c`. This means that `program_counter += 3` might also overflow into a negative value, halting execution. Just something to keep in mind.

There is no separation between instructions and data. They all reside in the same memory. The juxtaposition of 3 bytes of data together form an `xand` instruction, so really data & instructions are one and the same. It's usually easiest to treat data as single byte values, since you're limited to manipulating one byte of memory at a time via `xand`'s destructive subtraction. `xand` might be used to twiddle bits in some [unreachable](https://en.wikipedia.org/wiki/Unreachable_code) portion of memory sectioned off "just for data", or it could give rise to [self-modifying code](https://en.wikipedia.org/wiki/Self-modifying_code). Use your imagination. :rainbow:

### Compiler

The VM's "bytecode" is literally just a sequence of bytes. While you could input these with [a magnetized needle and a steady hand](https://xkcd.com/378/), we all agree that even machine code is easier with a *little* bit of gussying up.

The language implemented by the xand compiler is a thin veneer on the underlying instructions. You can write your code much as you'd guess, so that

```
xand 10 20 30
xand -40 -50 -60
```

translates to

| address | contents          |
----------|--------------------
| 0       | `0000 1010` (+10) |
| 1       | `0001 0100` (+20) |
| 2       | `0001 1110` (+30) |
| 3       | `1101 1000` (-40) |
| 4       | `1100 1110` (-50) |
| 5       | `1100 0100` (-60) |
| 6       | `0000 0000`       |
| ...     | ...               |
| 127     | `0000 0000`       |

Straight data literals are also supported; you could just as well write the above as

```
10 20 30
-40
-50
-60
```

since `xand` is just the juxtaposition of 3 consecutive bytes of data. (Notice you're free to use any whitespace as separators, too.)

But the real convenience comes from *labels*. Labels:
- must be lowercase;
- must start with a letter or an underscore; and
- may contain letters (`a`-`z`), underscores (`_`), or numbers (`0`-`9`).

To define a label, prefix any given `xand` or raw data with the label's name followed by a colon like so

```
foo: xand 10 20 30
_bar:
-40
-50
_bar2: -60
```

The above again compiles to the same result - labels confer no special meaning on the bytecode level. (What's more, notice that `-50` is unlabeled above.)

Labels come in handy because you can use them as operands to `xand` instructions. For example,

```
foo: xand bar baz qux
qux: xand -1 -1 -1
bar: 10
baz: 20
```

compiles to

| address | contents                               |
----------|-----------------------------------------
| 0       | `0000 0110` (6 = the address of `bar`) |
| 1       | `0000 0111` (7 = the address of `baz`) |
| 2       | `0000 0011` (3 = the address of `qux`) |
| 3       | `1111 1111` (-1)                       |
| 4       | `1111 1111` (-1)                       |
| 5       | `1111 1111` (-1)                       |
| 6       | `0000 1010` (+10)                      |
| 7       | `0001 0100` (+20)                      |
| 8       | `0000 0000`                            |
| ...     | ...                                    |
| 127     | `0000 0000`                            |

Notice that labels do not need to be defined before they are used.

Finally, since it's a common case, you can supply the special label `...` as the third operand to an `xand` to refer implicitly to the next instruction. So instead of saying

```
foo: xand a b bar
bar: xand x y baz
baz: xand -1 -1 -1
```

you can just say

```
xand a b ...
xand x y ...
xand -1 -1 -1
```

You can still of course use `...` even if the next instruction has an explicit label. Mix & match however you'd like.

To see this language in action, check out the Fibonacci example in [fib.xand](TODO).

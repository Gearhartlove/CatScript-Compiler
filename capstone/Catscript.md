# Catscript Guide
// note : use tests for example

This document should be used to create a guide for catscript, to satisfy capstone requirement 4

## Introduction

## Features

### CatScript Typing
#### Booleans
True and false represents the logical `true` and `false` respectively.
```js
true  // evaluates to logical true
false // evaluates to logical false 
```
#### Integers
The 32 bit signed `integer` type.
```js
-100 
0
100
```
#### Strings
A UTF-8-encoded, growable string. Supports string concatentation. 
##### Examples
You can create a `string` using the "word" syntax.
```js
"Hello"
```
You can concatenate strings using the `+` operator.
```js
"Hello World" == "Hello " + "World" // evaluates to true
```
#### Null
Conventional null type. Used when a variable is not instantiated or set to another type.
```js
null
```
##### Examples
You can set a value to null, and conditionally check if a variable is null. The null type does not support addition or any other operation. 
```js
var x = null
(x == null)  // evaluates to true
print(x + 1) // incompatable types exception 
```
#### Object
Wraps a primitive type to abstract type information. The common use case is when ther e is a `List` of `objects`.
##### Example
```js
var my_list: list<object> = ["hello", 3, null]
```
#### Void
The default return type of a function that does not explicitly return something. Returns the equivelent of nothing. Note, this is different than null.
##### Example
```js
function foo() {} // returns nothing (also know as void)
function add_one(x: int) : int { return x + 1} // returns and integer (x+1)
```
#### Lists
An immutible collection of the same type. 
#### Example
```js
[1,2,3] // list literal expression 
var my_string_list: list<string> = ["hello", "world"] // statement with list type
var my_object_list: list<object> = [null, 3, "foo"]
```
### CatScript Operations
#### Adding
Add two factor expressions together with the binary `+` operator.
####Example
```js
1 + 1 // evaluates to 2
4 * 5 + 3 // evaulautes to (4*5)+3 -> 20+3 -> 23
```
#### Subtracting
Subtract two factor expressions together with the binary `-` operator.
```js
1 - 1 // evaluates to 0
(4*5) - 3 // evaluates to (4*5)-3 -> 20-3 -> 17
```
#### Multiplication
Multiply two logical expressions together with binary `*` operator.
```js
1 * 8 // evaluates to 8
1 * 2 * 3 // evaluates to 6
```
#### Division
```js
6 / 3  // evaluates to 2
6 / 4  // evaluates to 1
6 / 10 // evaluates to 0
```
### For loops
### If Statements
### Print Statements
### Functions
#### Definitions
#### Function Calls
#### Returning Values
### Variables
#### Declaration
#### Assignment

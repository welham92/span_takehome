# Intro

Hi, I'm Matt! Thanks for taking a squizz at this, and apologies in advance for all the ramblings below.

# Building and running

I used Java 17, hope that's fine! Haven't checked but would probably work on Java 9+ (definitely used a `List.of()` somewhere).

Maven was used for building, so you'll need that, but all the required dependencies should be pulled and jar packaged with the following (from within `span_takehome` dir):

`mvn clean package`

(automated tests are also run with that phase)

To run the program use either of the following (sample argument provided is assignment's test file, change at your leisure) :

`java -jar target/span_takehome-1.0-jar-with-dependencies.jar src/test/resources/sample1.txt`

`mvn exec:java -Dexec.mainClass=com.spandigital.matt.CmdLineRunner -Dexec.args="src/test/resources/sample1.txt"`

# Assumptions, design decisions, and liberties taken

- If ANY of the lines are malformed (e.g. missing team name or score) then NO partial result will be printed.

I wouldn't want anyone (accidentally) glossing over an 'errors' section and simply grabbing a partial (and likely incorrect) result. So if there are errors, we'll only show those.


- We parse the whole file and accumulate all potential errors, rather than 'failing fast'. Line numbers and contents of faulty lines are printed, but not details on the specific fault.

This was an arbitrary choice. If we needed to fail fast then we could change to that, or even have a command line flag to switch between options.

The specific errors aren't printed for the line in the interest of keeping the assignment shorter and less bloated of my arbitrary choices, but they wouldn't be difficult to add.


- Error messages (e.g. file not found) are displayed on stderr.

Could hide them (perhaps based on cmdline flag), direct them to a log file, or whatnot depending on exact requirements. 


- No 'logging'.

In the interest of keeping things short and simple we're not writing any log files or using an associated library.


- Output goes to stdout

Arbitrary choice. Shrug!


- Only supply one cmdline argument - the file path

Arbitrarily fail the run if more than one argument is received, rather than potentially try and go ahead and give the user a false impression of 'success' under their terms.


- No libraries were used for command line argument parsing

Figured I'd keep things simple for the single-file argument, but would probably reach for a library if I had to parse additional flags and inputs.


- Assuming no commas in team names

If there are, we should either try and clean that up beforehand, or use a different delimiter, or change input file format (e.g. json or xml)


- String comparison on names is case-sensitive.

Arbitrary choice, can change if required. Means that both "Ants" and "ants" teams can coexist. Currently, Ants > Badgers > ants > badgers.


- In the case of success, only the ranked lines are shown (no "yay success" preamble), so that lines are homogeneous for potential piping to another program.

- Seeing as we're returning/printing a list, happy to return an empty list. So empty input file was deemed fine.

- Empty lines in input file are considered faulty.

- Opted to use `longs` over `ints` in most places, but `ints` would probably be fine. Rather over- than under-cater?

- Scores proooobably aren't negative, but without further context I've left them supported.

- Leading and trailing whitespace on team names is trimmed, and empty-string team name is NOT supported.

- Avoided making tons of indirection mini-methods to do simple things, or defining constants where they're only used in one place (or closely together).

- Assumed that a team can't play itself, but will depend on the context!

- I like Java Streams and a general 'functional' flavour, but I'd fall in line any other company best-practice if the approach grosses you out!

- Only (compile-time) dependency pulled in was io.vavr

I mainly wanted the `Either` class (similar to `Maybe` in other langs) so that I could bundle success and error responses into a single well-defined object, but having Tuples let me avoid creating a `Pair` record too!
#!/bin/sh

# .pm is also an extension used by Raku. Rename *.pm to *.prism
find . -name "*.pm" 2> /dev/null -exec echo "Renaming {}" && rename .pm .prism '{}' \;
# .sm is also used by Smalltalk
find . -name "*.sm" 2> /dev/null -exec echo "Renaming {}" && rename .sm .prism '{}' \;

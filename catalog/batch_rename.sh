#!/bin/sh

# .pm is also an extension used by Raku. Rename *.pm to *.prism
find . -iname "*pm" -exec rename -v 's/\.pm$/.prism/' '{}' \;
# .sm is also used by Smalltalk
find . -iname "*sm" -exec rename -v 's/\.sm$/.prism/' '{}' \;

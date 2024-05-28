---
title: "Three-Reaction Example"
...

# Model Description

A simple system of three species and three
reactions. The species are $A$, $B$ and $C$. The reactions are:

```{=latex}
\begin{align*}
  \emptyset &\xrightarrow{k_1} A & [R1]\\
  \emptyset &\xrightarrow{k_2} B & [R2]\\
  A+B       &\xrightarrow{k_3} C & [R3]
\end{align*}
```

This model is part of each of the provided PRISM model files.


# Properties

The property of interest is the probability to reach an *objective*
state before a given time limit, $T$. The *objective* state is
identified using a label in the PRISM model:

```
label "objective" = a=0 & b=0 & c=2; 
```

Then, in PRISM syntax, the CSL property is given as `P=? [F[0,T] "objective"]`. 


# Analysis

This model has infinite states (in principle) but the objective is
unreachable if $A>2$ or $B>2$ or $C>2$. This is visually clear in the
state diagram:

![State diagram for the three-reaction model. The objective (red) is
not achievable from the gray shaded states, so they are treated as
absorbing states.](figures/three_reaction.pdf)

The absorbing states are indicated using a `constraint` label in the
PRISM model file:

```
label "constraint" = a+c<maxConstraint & b+c<maxConstraint;
```

The `constraint` is controlled by the `maxConstraint` constant. The
lowest valid setting of `maxConstraint` is 3. The "constrained"
importance sampling heuristic works by rejecting any transitions that
violate the `constraint`. When `maxConstrain` is 3, all simulated
paths reach the objective, which is optimum. When `maxConstraint` is
greater than 3, simulated paths can terminate in absorbing states. 



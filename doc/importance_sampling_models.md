---
title: 'Importance Sampling on CTMC Models'
...

# Introduction

This project contains Java source and PRISM model 
files used to study importance sampling methods in
Continuous Time Markov Chain (CTMC) models. In past
literature, significant work was done on IS for special
sub-classes of CTMC models, for instance Mean Time to
Fail estimation in regenerative models that always 
return to their initial state.

One of our goals is to develop novel IS approaches
for CTMC models, including non-regenerative models.
A collection of "abstract" models is provided using
the PRISM language. Each N-state model has the 
following characteristics:

* States are indexed 1, 2, ..., N
* State 1 is the initial state
* State N has no exiting transitions and is
  considered the "target" or terminal state.
* States 1, ..., N-1 all have transitions
  leading to state N, and are fully 
  connected to each other
* Between any states a,b there is a transition 
  with rate Ra_b, where a is in 1,...,N-1 and
  b is in 1,...,N.

The abstract models are fully-connected, and can be 
pruned by setting select edge transition rates to 
zero. In each model, state N is considered a "target" 
state, and the IS objective is to estimate the probability
and/or mean time to reach state N.


# Two-State Model

```
models/abstract/2_states/2_state.pm
```

![Trivial two-state model.](../models/abstract/2_states/2_state.png)

The trivial two-state model has only one possible transition 
which occurs with probability 1. There is a single path.



# Three-State Model

```
models/abstract/3_states/3_state.pm
```

![Three-state model.](../models/abstract/3_states/3_state.png)

A general three-state model has two non-regenerative 
paths that terminate in state 3:

* 1 3
* 1 2 3

There is also a single regenerative path:

* 1 2 1

The probability to reach state 3 before returning to state 1 
is 

$$ \frac{\frac{R_{13}}{R_{12}+R_{13}} + \frac{R_{12}}{R_{12}+R_{13}}\frac{R_{23}}{R_{23}+R_{21}}}{\frac{R_{12}}{R_{12}+R_{13}}\frac{R_{21}}{R_{23}+R_{21}}} $$
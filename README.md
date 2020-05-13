# Numerical methods for integer factorization and discrete logarithm
Author: David Salac <http://www.github.com/david-salac>

A simple implementation of common solvers of two fundamental cryptographical
problems: discrete logarithm and integer factorization.

Methods are implemented using Java programming language.

## Introduction
Discrete logarithm and integer factorization are the common ground of the
public-key cryptography. All commonly used methods rely on the expected
computational complexity required for solving these problems. In other words,
there is no known algorithm that can solve them in the polynomial-time (on
normal, not a quantum computer). However, many algorithms are much faster
than brute force (work in subexponential time).

This repository is the entering point for studying the fundamental
cryptoanalytical methods for breaking of general public-key problems (based on
discrete logarithm or integer factorization).

## Integer factorization
Generally speaking, it is the task to find a decomposition of the natural
number _n_ to the product of prime numbers (and its exponent). It means for
positive integer _n_, find decomposition:

_n_ = _p_<sub>1</sub><sup>_e_<sub>1</sub></sup> ⋅
_p_<sub>2</sub><sup>_e_<sub>2</sub></sup> ⋅ ... ⋅
_p_<sub>k</sub><sup>_e_<sub>k</sub></sup>

for _p<sub>i</sub>_ prime number and _e<sub>i</sub>_ natural number (for all
_i_ in range 1, ..., _k_).

### Presented methods
This repository presents basic methods like Dixon's random squares method,
quadratic sieve algorithm (QS), general/specific number field sieve (GNFS,
SFNS, NFS). It also presents other trivial methods for solving this problem.

## Discrete logarithm
It is the task of finding number _p_ such for selected integers _a_, _b_ and
modulus _n_ (a positive integer), such that:

_a_ = _b_<sup>_p_</sup> mod _n_

Typically the _a_, _b_ and _n_ are known values and _p_ is the private key
that has to be computed.

### Presented methods
Far fewer methods are available for effective (sub-exponential) solving of
this problem. They are even less stable. This repository presents Index
Calculus method and other less effective methods (like Baby-step Giant-step
method, Silver-Pohling-Hellman method). 

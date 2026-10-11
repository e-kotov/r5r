#' @param random_seed A whole number between -2147483647 and 2147483647, or
#'   `NULL` (the default). Sets the R5 random seed for Monte Carlo frequency
#'   schedules and departure-time sampling. The same seed reproduces the draws
#'   with the same inputs and R5 version; it takes precedence over
#'   `lock_schedules`. Each origin uses this seed independently. This controls
#'   Java's RNG, independently of R's `set.seed()`. Requires an R5 JAR with
#'   `randomSeed` and `lockSchedules` support, selected with
#'   `options(r5r.r5jar = "path/to/r5.jar")` before starting Java. The default
#'   preserves R5's existing behavior. Output row order and the ordering of
#'   equivalent itineraries may vary. Expanded and arrival matrices order
#'   paths deterministically when either seed control is enabled.
#' @param lock_schedules A logical. Defaults to `FALSE`. When `TRUE` and
#'   `random_seed` is `NULL`, R5 derives the seed from the origin latitude to
#'   reproduce frequency schedules. Requires the same seed-supporting R5 JAR
#'   as `random_seed`. These settings apply only to the current call.

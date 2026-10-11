testthat::skip_on_cran()

test_that("Monte Carlo controls validate inputs and detect R5 support", {
  core <- spo_network@jcore
  on.exit(core$resetRoutingProperties())
  for (seed in list(NA, Inf, TRUE, "42", c(1, 2), 1.5, -2147483648, 2147483648)) {
    expect_error(set_monte_carlo_seed(core, seed, FALSE))
  }
  for (lock in list(NA, 1, "TRUE", c(TRUE, FALSE))) {
    expect_error(set_monte_carlo_seed(core, NULL, lock))
  }
  supported <- tryCatch({
    set_monte_carlo_seed(core, 42, FALSE)
    TRUE
  }, error = function(e) FALSE)
  if (supported) {
    for (seed in c(-2147483647, 0, 2147483647)) {
      expect_true(set_monte_carlo_seed(core, seed, FALSE))
    }
    expect_true(set_monte_carlo_seed(core, NULL, TRUE))
  } else {
    expect_error(set_monte_carlo_seed(core, 42, FALSE), "require an R5 JAR")
    expect_error(set_monte_carlo_seed(core, NULL, TRUE), "require an R5 JAR")
  }
  expect_true(set_monte_carlo_seed(core, NULL, FALSE))
})

test_that("seeded frequency draws repeat and controls do not leak between calls", {
  core <- spo_network@jcore
  supported <- tryCatch({
    set_monte_carlo_seed(core, 42, FALSE)
    TRUE
  }, error = function(e) FALSE)
  core$resetRoutingProperties()
  skip_if(!supported, "Requires a custom R5 JAR with Monte Carlo seed support")
  expect_true(core$hasFrequencies())
  args <- list(
    r5r_network = spo_network, origins = spo_points[51:53],
    destinations = spo_points[56:70], mode = c("WALK", "TRANSIT"),
    departure_datetime = departure_datetime, time_window = 10L,
    max_trip_duration = 60L, draws_per_minute = 3L,
    breakdown = TRUE, n_threads = 1L, progress = FALSE
  )
  run <- function(...) {
    as.data.frame(do.call(expanded_travel_time_matrix, c(args, list(...))))
  }
  first <- run(random_seed = 42)
  expect_gt(nrow(first), 0)
  expect_true(any(first$n_rides > 0, na.rm = TRUE))
  expect_identical(first, run(random_seed = 42))
  expect_false(identical(first, run(random_seed = 43)))
  args$n_threads <- 2L
  expect_identical(first, run(random_seed = 42))
  locked <- run(lock_schedules = TRUE)
  expect_identical(locked, run(lock_schedules = TRUE))
  expect_identical(first, run(random_seed = 42, lock_schedules = TRUE))
  expect_false(identical(run(), run()))
})

test_that("the other stochastic routing APIs reproduce seeded results", {
  supported <- tryCatch({
    set_monte_carlo_seed(spo_network@jcore, 42, FALSE)
    TRUE
  }, error = function(e) FALSE)
  spo_network@jcore$resetRoutingProperties()
  skip_if(!supported, "Requires a custom R5 JAR with Monte Carlo seed support")
  args <- list(
    r5r_network = spo_network, origins = spo_points[51:53],
    destinations = spo_points[56:70], mode = c("WALK", "TRANSIT"),
    departure_datetime = departure_datetime, time_window = 10L,
    max_trip_duration = 60L, n_threads = 2L, progress = FALSE,
    random_seed = 42
  )
  cases <- list(
    travel_time_matrix = list(percentiles = c(1L, 50L, 99L)),
    arrival_travel_time_matrix = list(arrival_datetime = departure_datetime + 3600,
                                      breakdown = TRUE),
    accessibility = list(opportunities_colnames = "opportunities", cutoffs = 30),
    pareto_frontier = list(fare_structure = spo_fare_struc,
                           fare_cutoffs = c(0, 5, 10, Inf)),
    detailed_itineraries = list(r5r_network = r5r_network,
                                origins = pois[1:2], destinations = pois[3:4],
                                all_to_all = TRUE, drop_geometry = TRUE,
                                fare_structure = fare_structure, max_fare = 10),
    travel_time_surface = list(zoom = 9L),
    isochrone = list(zoom = 9L, cutoffs = c(0, 15, 30), progress = FALSE)
  )
  for (name in names(cases)) {
    call_args <- args
    call_args[names(cases[[name]])] <- cases[[name]]
    if (name == "arrival_travel_time_matrix") {
      call_args[c("departure_datetime", "time_window")] <- NULL
    }
    if (name %in% c("travel_time_surface", "isochrone")) {
      call_args$destinations <- NULL
    }
    first <- do.call(get(name), call_args)
    expect_gt(if (is.data.frame(first)) nrow(first) else length(first), 0)
    expect_identical(first, do.call(get(name), call_args), info = name)
  }
})

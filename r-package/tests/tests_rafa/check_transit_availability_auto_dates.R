# Try automatic operating-date discovery with r5r's bundled Porto Alegre data.
# Run from the repository root with: Rscript r-package/tests/tests_rafa/check_transit_availability_auto_dates.R
# The first run may take a few minutes while build_network() creates network.dat.

run_example <- function() {
  options(java.parameters = "-Xmx2G")
  rJavaEnv::use_java(21)
  suppressPackageStartupMessages(library(r5r))

  cat("r5r version:", as.character(utils::packageVersion("r5r")), "\n")
  sample_path <- system.file("extdata", "poa", package = "r5r")
  if (!nzchar(sample_path)) {
    stop("Could not find r5r's bundled Porto Alegre data.")
  }
  data_path <- tempfile("r5r-poa-example-")
  dir.create(data_path)
  if (
    !all(file.copy(
      list.files(sample_path, full.names = TRUE),
      data_path,
      recursive = TRUE
    ))
  ) {
    stop(
      "Could not copy the bundled Porto Alegre data to a temporary directory."
    )
  }
  on.exit(unlink(data_path, recursive = TRUE), add = TRUE)

  network <- build_network(data_path = data_path)
  on.exit(stop_r5(network), add = TRUE)

  # Leave all date arguments out to discover every operating date.
  availability <- check_transit_availability(network)
  print(availability)

  if (nrow(availability) > 0L) {
    cat(
      "\nDiscovered",
      nrow(availability),
      "operating dates from",
      format(min(availability$date)),
      "through",
      format(max(availability$date)),
      "\n"
    )

    # Check the discovery result against explicit queries for the same dates.
    explicit <- check_transit_availability(network, dates = availability$date)
    stopifnot(isTRUE(all.equal(
      availability,
      explicit,
      check.attributes = FALSE
    )))
    cat("Automatic and explicit-date results match.\n")
  } else {
    cat("This network has no retained transit trips with operating dates.\n")
  }
}

run_example()

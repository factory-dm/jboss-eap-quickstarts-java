#!/bin/bash
#
# JBoss EAP Quickstarts - Accessibility Testing Script
#
# This script automates the deployment and accessibility testing of JBoss EAP quickstarts.
# It runs the AccessibilityTest class against each deployed quickstart and generates a report.
#
# Usage: ./test-accessibility.sh [options]
#
# Options:
#   -s, --server-home    Path to JBoss EAP server home directory (default: $JBOSS_HOME)
#   -p, --port           Server HTTP port (default: 8080)
#   -q, --quickstarts    Comma-separated list of quickstarts to test (default: all)
#   -r, --report-dir     Directory to store reports (default: ./accessibility-reports)
#   -h, --help           Show this help message
#

set -e

# Default values
SERVER_HOME=${JBOSS_HOME:-""}
SERVER_PORT=8080
QUICKSTARTS_DIR=$(dirname $(dirname $(readlink -f "$0")))
REPORT_DIR="$QUICKSTARTS_DIR/accessibility-reports"
QUICKSTARTS_TO_TEST="all"
CHROME_DRIVER_PATH=""

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    -s|--server-home)
      SERVER_HOME="$2"
      shift 2
      ;;
    -p|--port)
      SERVER_PORT="$2"
      shift 2
      ;;
    -q|--quickstarts)
      QUICKSTARTS_TO_TEST="$2"
      shift 2
      ;;
    -r|--report-dir)
      REPORT_DIR="$2"
      shift 2
      ;;
    -d|--chromedriver)
      CHROME_DRIVER_PATH="$2"
      shift 2
      ;;
    -h|--help)
      echo "Usage: $0 [options]"
      echo ""
      echo "Options:"
      echo "  -s, --server-home    Path to JBoss EAP server home directory (default: \$JBOSS_HOME)"
      echo "  -p, --port           Server HTTP port (default: 8080)"
      echo "  -q, --quickstarts    Comma-separated list of quickstarts to test (default: all)"
      echo "  -r, --report-dir     Directory to store reports (default: ./accessibility-reports)"
      echo "  -d, --chromedriver   Path to ChromeDriver executable"
      echo "  -h, --help           Show this help message"
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      echo "Use --help for usage information"
      exit 1
      ;;
  esac
done

# Validate required parameters
if [ -z "$SERVER_HOME" ]; then
  echo "ERROR: JBoss EAP server home directory not specified."
  echo "Please set JBOSS_HOME environment variable or use --server-home option."
  exit 1
fi

if [ ! -d "$SERVER_HOME" ]; then
  echo "ERROR: JBoss EAP server home directory does not exist: $SERVER_HOME"
  exit 1
fi

if [ -z "$CHROME_DRIVER_PATH" ]; then
  echo "WARNING: ChromeDriver path not specified. Will use system PATH to find chromedriver."
  CHROME_DRIVER_PARAM=""
else
  if [ ! -x "$CHROME_DRIVER_PATH" ]; then
    echo "ERROR: ChromeDriver not found or not executable: $CHROME_DRIVER_PATH"
    exit 1
  fi
  CHROME_DRIVER_PARAM="-Dwebdriver.chrome.driver=$CHROME_DRIVER_PATH"
fi

# Create report directory
mkdir -p "$REPORT_DIR"
echo "Reports will be saved to: $REPORT_DIR"

# Function to check if server is running
check_server() {
  echo "Checking if JBoss EAP server is running..."
  if curl -s "http://localhost:$SERVER_PORT" > /dev/null; then
    echo "Server is running on port $SERVER_PORT"
    return 0
  else
    echo "ERROR: JBoss EAP server is not running on port $SERVER_PORT"
    echo "Please start the server before running this script."
    return 1
  fi
}

# Function to get list of quickstarts to test
get_quickstarts_list() {
  if [ "$QUICKSTARTS_TO_TEST" = "all" ]; then
    # Find all directories that have a pom.xml and src/main/webapp
    find "$QUICKSTARTS_DIR" -maxdepth 1 -type d | while read dir; do
      if [ -f "$dir/pom.xml" ] && [ -d "$dir/src/main/webapp" ]; then
        basename "$dir"
      fi
    done | grep -v "shared-doc\|shared-templates" | sort
  else
    echo "$QUICKSTARTS_TO_TEST" | tr ',' ' '
  fi
}

# Function to deploy a quickstart
deploy_quickstart() {
  local quickstart=$1
  echo "Deploying $quickstart..."
  
  cd "$QUICKSTARTS_DIR/$quickstart"
  
  # Build and deploy using Maven
  mvn clean package wildfly:deploy -DskipTests
  
  if [ $? -ne 0 ]; then
    echo "ERROR: Failed to deploy $quickstart"
    return 1
  fi
  
  echo "Successfully deployed $quickstart"
  return 0
}

# Function to run accessibility tests for a quickstart
run_accessibility_tests() {
  local quickstart=$1
  local context_path=$quickstart
  local report_file="$REPORT_DIR/${quickstart}-accessibility-report.html"
  
  echo "Running accessibility tests for $quickstart..."
  
  cd "$QUICKSTARTS_DIR/$quickstart"
  
  # Determine the context path (might be different from directory name)
  if [ -f "src/main/webapp/WEB-INF/jboss-web.xml" ]; then
    context_path=$(grep -oP '(?<=<context-root>).*(?=</context-root>)' src/main/webapp/WEB-INF/jboss-web.xml)
  fi
  
  # Run the AccessibilityTest with the appropriate parameters
  mvn test -Dtest=AccessibilityTest \
    -Dbaseurl="http://localhost:$SERVER_PORT/$context_path" \
    -Dit-tests \
    $CHROME_DRIVER_PARAM \
    -Dsurefire.reportFormat=html \
    -Dsurefire.outputDirectory="$REPORT_DIR/$quickstart"
  
  test_result=$?
  
  if [ $test_result -eq 0 ]; then
    echo "Accessibility tests PASSED for $quickstart"
    echo "<tr class=\"success\"><td>$quickstart</td><td>PASSED</td><td><a href=\"$quickstart/index.html\">View Report</a></td></tr>" >> "$REPORT_DIR/results.html.part"
  else
    echo "Accessibility tests FAILED for $quickstart"
    echo "<tr class=\"failure\"><td>$quickstart</td><td>FAILED</td><td><a href=\"$quickstart/index.html\">View Report</a></td></tr>" >> "$REPORT_DIR/results.html.part"
  fi
  
  return $test_result
}

# Function to undeploy a quickstart
undeploy_quickstart() {
  local quickstart=$1
  echo "Undeploying $quickstart..."
  
  cd "$QUICKSTARTS_DIR/$quickstart"
  
  # Undeploy using Maven
  mvn wildfly:undeploy
  
  if [ $? -ne 0 ]; then
    echo "WARNING: Failed to undeploy $quickstart"
  else
    echo "Successfully undeployed $quickstart"
  fi
}

# Function to generate summary report
generate_summary_report() {
  local report_file="$REPORT_DIR/index.html"
  local timestamp=$(date "+%Y-%m-%d %H:%M:%S")
  local total_quickstarts=$(wc -l < "$REPORT_DIR/results.html.part")
  local passed_quickstarts=$(grep -c "class=\"success\"" "$REPORT_DIR/results.html.part")
  local failed_quickstarts=$((total_quickstarts - passed_quickstarts))
  
  echo "Generating summary report..."
  
  cat > "$report_file" << EOL
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>JBoss EAP Quickstarts - Accessibility Test Report</title>
  <style>
    body {
      font-family: Arial, sans-serif;
      line-height: 1.6;
      margin: 0;
      padding: 20px;
      color: #333;
    }
    h1, h2 {
      color: #cc0000;
    }
    .summary {
      background-color: #f8f8f8;
      border: 1px solid #ddd;
      border-radius: 4px;
      padding: 15px;
      margin-bottom: 20px;
    }
    table {
      border-collapse: collapse;
      width: 100%;
      margin-top: 20px;
    }
    th, td {
      text-align: left;
      padding: 12px;
      border-bottom: 1px solid #ddd;
    }
    th {
      background-color: #f2f2f2;
    }
    tr.success td {
      background-color: #dff0d8;
    }
    tr.failure td {
      background-color: #f2dede;
    }
    tr:hover {
      background-color: #f5f5f5;
    }
    .footer {
      margin-top: 30px;
      font-size: 0.8em;
      color: #777;
    }
  </style>
</head>
<body>
  <h1>JBoss EAP Quickstarts - Accessibility Test Report</h1>
  
  <div class="summary">
    <h2>Summary</h2>
    <p><strong>Date:</strong> $timestamp</p>
    <p><strong>Total Quickstarts Tested:</strong> $total_quickstarts</p>
    <p><strong>Passed:</strong> $passed_quickstarts</p>
    <p><strong>Failed:</strong> $failed_quickstarts</p>
  </div>
  
  <h2>Detailed Results</h2>
  <table>
    <thead>
      <tr>
        <th>Quickstart</th>
        <th>Status</th>
        <th>Report</th>
      </tr>
    </thead>
    <tbody>
EOL

  cat "$REPORT_DIR/results.html.part" >> "$report_file"
  
  cat >> "$report_file" << EOL
    </tbody>
  </table>
  
  <div class="footer">
    <p>Generated by test-accessibility.sh script on $timestamp</p>
  </div>
</body>
</html>
EOL

  echo "Summary report generated: $report_file"
}

# Main execution

# Check if server is running
check_server || exit 1

# Initialize results file
echo "" > "$REPORT_DIR/results.html.part"

# Get list of quickstarts to test
QUICKSTARTS=$(get_quickstarts_list)
echo "Testing the following quickstarts: $QUICKSTARTS"

# Test each quickstart
overall_result=0
for quickstart in $QUICKSTARTS; do
  echo "===== Processing $quickstart ====="
  
  if deploy_quickstart "$quickstart"; then
    # Give the deployment a moment to initialize
    sleep 5
    
    # Run accessibility tests
    run_accessibility_tests "$quickstart"
    test_result=$?
    
    # If any test fails, set overall result to failure
    if [ $test_result -ne 0 ]; then
      overall_result=1
    fi
    
    # Undeploy after testing
    undeploy_quickstart "$quickstart"
  else
    echo "<tr class=\"failure\"><td>$quickstart</td><td>DEPLOYMENT FAILED</td><td>N/A</td></tr>" >> "$REPORT_DIR/results.html.part"
    overall_result=1
  fi
  
  echo ""
done

# Generate summary report
generate_summary_report

# Clean up
rm -f "$REPORT_DIR/results.html.part"

echo "===== Accessibility Testing Complete ====="
if [ $overall_result -eq 0 ]; then
  echo "All tests passed!"
else
  echo "Some tests failed. See the report for details: $REPORT_DIR/index.html"
fi

exit $overall_result

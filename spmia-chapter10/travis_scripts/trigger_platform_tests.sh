echo "Beginning platform tests for build $BUILD_NAME"
# 깃허브 토큰을 사용해 Travis CI에 로그인하고 반환된 OAuth2 토큰을 RESULTS 변수에 저장한다.
travis login --org --no-interactive  --github-token $GITHUB_TOKEN
export RESULTS=`travis token --org`
export TARGET_URL="https://api.travis-ci.org/repo/carnellj%2F$PLATFORM_TEST_NAME/requests"
echo "Kicking off job using target url: $TARGET_URL"

body="{
\"request\": {
  \"message\": \"Initiating platform tests for build $BUILD_NAME\",
  \"branch\":\"master\",
  \"config\": {
    \"env\": {
      \"global\": [\"BUILD_NAME=$BUILD_NAME\",
                   \"CONTAINER_IP=$CONTAINER_IP\"]
    }
  }
}}"

echo "$body"

curl -s -X POST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "Travis-API-Version: 3" \
  -H "Authorization: token $RESULTS" \
  -d "$body" \
  $TARGET_URL

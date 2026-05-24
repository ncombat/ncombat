# Spring Boot 2.2.6 → 3.5.14 Upgrade Plan (Java 8 → 21)

## Context

The active build is pinned to Spring Boot **2.2.6.RELEASE** and Java **1.8** (this part is verified — see `pom.xml:11` and `pom.xml:20`). The 2.2.x line reached EOL some time around 2020 and the 2.2.6 release date is around April 2020 — **dates not verified against the official Spring release matrix; please confirm before quoting them externally**. The recent security review (`security_analysis.md`) flagged the EOL Boot version as the highest-severity finding because the 2.2.x line carries a long tail of known CVEs. Moving to Spring Boot **3.5.14** on a current LTS JDK is expected to clear that tail in one move and unlock Spring Security 6, Tomcat 10.x, and a modern Jackson/Logback — all of which are prerequisites for the follow-up security work tracked separately. **The current support status of Spring Boot 3.5.14 specifically is past my August 2025 knowledge cutoff; verify it is still a supported patch on the 3.5 line before pinning to it.**

The codebase is small (≈15 Java files in `org.ncombat`, 5 test classes, 3 Thymeleaf templates, no logging config, no Spring Security, no JPA, no custom WebMvc beans). Exploration found only **two** Servlet-API imports and **two** deprecated boxing constructors in the entire main source tree. There are no `WebMvcConfigurerAdapter`, `WebSecurityConfigurerAdapter`, `HandlerInterceptorAdapter`, or other Spring patterns that were removed in 6.x. The reflective command dispatch in `CommandParser` and `Combatant` uses `getMethod()`/`invoke()` on public methods within the same package and needs no `--add-opens`.

**Decisions confirmed with the user:**
- JDK target: **Java 21** (latest LTS, fully supported by Boot 3.5)
- Tests: keep JUnit 4 by adding `junit-vintage-engine` explicitly
- Security findings: out of scope for this PR; addressed in a follow-up
- Legacy `bin/` Maven module: **delete**

**Baseline established (verified by running):**
- Active JDK on this machine is OpenJDK 21.0.10. Source level is still `1.8` per `pom.xml:20`, but compilation under JDK 21 works today.
- Maven wrapper resolves to 3.6.3.
- `./mvnw clean test` is green: 5 test classes, **69 tests, 0 failures, 0 errors, 0 skipped**. This is the regression baseline for the upgrade.
- Compiler emits one deprecation-for-removal warning (`VectorTest.java`'s `junit.framework.TestCase` import). No errors.

## Files to Modify

### Build (the only substantial change)

**`pom.xml`** — root build:
- Parent: `spring-boot-starter-parent` `2.2.6.RELEASE` → `3.5.14`
- `<java.version>`: `1.8` → `21`
- Add explicit `<dependency>` for `org.junit.vintage:junit-vintage-engine` with `scope=test` (Boot 3 no longer pulls it transitively from `spring-boot-starter-test`)
- Remove `<relativePath/>` only if present; otherwise leave the parent declaration's structure intact

**`.mvn/wrapper/maven-wrapper.properties`** — bump wrapper:
- `distributionUrl` to Maven `3.9.16` (Boot 3.5's documented floor is, I believe, Maven 3.6.3, with 3.9.x typically recommended — verify against the Spring Boot 3.5 release notes before pinning)

**Delete the legacy module** (per user decision):
- `bin/pom.xml`
- `bin/.gitignore`
- `bin/README.md`
- `bin/docs/` and `bin/src/` subtrees if they are pure duplicates of root (verify with a quick diff before `git rm -r bin/`)

### Source (mechanical edits)

**`src/main/java/org/ncombat/web/GameRestController.java`** — only file with Jakarta migration:
- Line 18: `import javax.servlet.http.HttpServletRequest;` → `import jakarta.servlet.http.HttpServletRequest;`
- Line 19: `import javax.servlet.http.HttpSession;` → `import jakarta.servlet.http.HttpSession;`

**`src/main/java/org/ncombat/command/CommandParser.java`** — deprecated boxing constructors:
- Line 456: `result = new Double(text);` → `result = Double.valueOf(text);`
- Line 474: `result = new Integer(text);` → `result = Integer.valueOf(text);`

(Keeping `valueOf` rather than `parseDouble`/`parseInt` preserves the `Double`/`Integer` boxed return type that the existing call sites expect.)

**`src/main/webapp/WEB-INF/web.xml`** — delete. This file is for the legacy WAR module only; Spring Boot does not read it. Leaving it after deleting `bin/` would be confusing.

### Source (optional diamond touch-ups — permitted in this PR)

The following are non-blocking cleanups in files we're already opening. They are **approved for inclusion in this PR** at the implementer's discretion (keep all, some, or none — whichever keeps the diff cleanest):

- `GameServer.java:254` — add diamond: `new ArrayList<>(commandBatches)`
- `AdminRestController.java:14`, `AppRestController.java:14` — `Map<String, Object> model = new HashMap<>();`
- Re-evaluate the class-level `@SuppressWarnings("unchecked")` on `GameServer`, `Combatant`, `CommandParser` after the diamond fixes — remove any that are no longer needed.

## What I Verified Won't Break

These were checked during exploration and **do not need changes**:

- `application.properties` — all `spring.thymeleaf.*` keys still valid; `server.port=5000`, `logging.level.*` unchanged.
- Templates `templates/ncombat.html`, `ncombat2.html`, `admin.html` — only simple `${var}` / `[[${var}]]` interpolation. Thymeleaf 3.1's restricted SpEL (no `T(...)`, no `new ClassName(...)`) is not exercised.
- Reflection sites (`CommandParser.java:89, 101, 134`, `Combatant.java:274-275, 284`) — `getMethod`/`invoke` on **public** methods. No JPMS opens required.
- `GameManager implements InitializingBean` / `GameServer implements DisposableBean` — both interfaces survive Spring 6 unchanged.
- Constructor injection (`GameRestController:35-37`) — already idiomatic Spring 5+.
- `GameStatusModel extends HashMap` — Jackson serialization of a `HashMap` subclass should be unchanged in the Jackson version that Boot 3.5 ships (I believe a 2.15+ line, but I did not confirm the exact version pinned by the 3.5.14 BOM — verify and smoke-test the JSON response shape).
- No `logback.xml`, `log4j2.xml`, `application.yml`, custom serializers, JAXB, SnakeYAML, JodaTime, or `sun.*` imports anywhere (verified by grep).
- No `Dockerfile`, no `.github/workflows/`, no `.github/dependabot.yml` to update.

## Potential Problems to Watch For

1. **JUnit Vintage Engine is now explicit.** I believe Spring Boot's `spring-boot-starter-test` stopped pulling `junit-vintage-engine` transitively at Boot 2.4 — but I have not double-checked which exact 2.x version dropped it. What is certain is that Boot 3 does not include it by default, so forgetting the explicit `<dependency>` will silently skip every existing test (Surefire would report "Tests run: 0"). Test the change with `./mvnw test` and confirm the 5 existing test classes still execute.

2. **`new Integer(text)` / `new Double(text)` are deprecated for removal.** Confirmed by the baseline run: these still **compile under Java 21** (deprecation-for-removal warning, not an error). The two-line fix in `CommandParser.java` is good hygiene and forward-compatibility, not a hard blocker.

3. **Tomcat default cookie behavior.** I have heard that Jakarta Tomcat 10.x defaults `HttpOnly=true` on the session cookie, which would be a small free win on security finding #7. **I did not verify this against the current Tomcat 10.1 release notes** — confirm by capturing a `Set-Cookie` response header after the upgrade. `Secure` and `SameSite` still need to be set explicitly in the follow-up security PR regardless.

4. **Trailing-slash URL matching defaults.** I believe Spring 6 / Boot 3 changed the default so that trailing-slash variants no longer match — but I have not re-verified this against the current Spring MVC docs. All controller mappings here use exact paths (`/game.do`, `/gameLogin.json`, etc.) and the JS clients call those exact paths, so even if my recollection is right, this codebase should be unaffected. Worth a smoke test of every endpoint regardless.

5. **Embedded Tomcat 10.1 is in a different package** (`org.apache.tomcat.embed`). No code in this project references Tomcat classes directly, so this is invisible — but if anyone adds custom `Filter`s or `Valve`s later, they must use the `jakarta.*` packages.

6. **`Integer.valueOf` / `Double.valueOf` semantics.** `new Integer("0042")` and `Integer.valueOf("0042")` are equivalent (both parse decimal). No behavioral difference. The same is true for `Double.valueOf` vs `new Double`. Test coverage in `CommandParserTest` already exercises these.

7. **IDE workspace cache.** `.idea/workspace.xml` is local-only (not tracked) but the IDE will refresh the project model after the parent POM changes. Expect a one-time re-import.

8. **Maven wrapper transitive metadata.** Bumping the wrapper URL is fine, but verify `./mvnw -v` after the change reports the new version on a clean machine (the wrapper jar is checked in but the distribution is downloaded on first run).

## Impact on the Existing `security_analysis.md`

This upgrade interacts with the security review as follows. Items not listed are unaffected.

**Directly resolved (expected, not verified):**
- **Finding #1 (Critical — Spring Boot 2.2.6 EOL CVEs):** Expected to be resolved. **I am assuming the 3.5.x line is still supported as of the date this upgrade actually happens — confirm against the Spring support matrix.** The specific CVEs cited in security finding #1 (Spring4Shell, SpEL DoS, the 2023 path-pattern CVEs, Tomcat request-smuggling CVEs, Jackson polymorphic CVEs) should all drop off the dependency graph; verify with `./mvnw org.owasp:dependency-check-maven:check` before and after to confirm the before/after delta.
- **Finding #15 (Info — legacy `bin/` module):** Resolved by deletion (verifiable directly from the diff).

**Partially mitigated (assumed, not verified):**
- **Finding #7 (Medium — cookie attributes):** Tomcat 10.x is believed to default `HttpOnly=true` (see "Potential Problems" #3 — not verified). `Secure` and `SameSite=Lax` still require explicit `server.servlet.session.cookie.*` configuration in the follow-up regardless.
- **Finding #16 (Info — `Math.random()`):** Not a blocker; Java 21's `RandomGenerator` API is available if anyone replaces it later. No change required now.

**Unchanged by the upgrade — still open after the PR lands:**
- #2 jQuery 1.3.2 (frontend asset; not bundled by Spring)
- #3 `eval()` of server response
- #4 unauthenticated `/admin.do` / `/adminTest.json`
- #5 missing CSRF
- #6 session fixation
- #8 message rendering via `innerHTML` / `.html()`
- #9 log injection via `requestedSessionId`
- #10 unauthenticated `/appTest.json`
- #11 `validateStopCommand` no-op
- #12 `CommandParser` cache-init race
- #13 no rate limiting
- #14 Google Analytics over mixed content

**Newly enabled by the upgrade** (expected; not all verified):
- Spring Security 6 is the natural way to fix #4, #5, and #6 at once. It requires Spring Boot 3+ (this part is established).
- The `server.servlet.session.cookie.same-site` property closes the SameSite gap from #7. **I am not certain at which Boot version this property was introduced — I believe it post-dates 2.2.6**, but verify by trying to set it on the current code first if you want a definitive before/after.
- Adding a CSP-header `Filter` works the same on 2.2 and on 3.5 — both versions can register a `Filter` bean. (An earlier draft of this plan claimed the upgrade made this easier; correcting that here.) The only real upgrade-driven benefit relevant to CSP work is that the broader Spring Security 6 integration becomes available.

## Verification

Run sequentially after the edits:

1. **Build clean:** `./mvnw clean package` — must succeed without `-DskipTests`.
2. **Tests run — match the baseline:** `./mvnw test` must report **5 test classes** (`CommandParserTest`, `CommandTextTest`, `CommandTokenizerTest`, `MotionComputerTest`, `VectorTest`) and **69 tests, 0 failures, 0 errors, 0 skipped**. If the count drops — especially to `Tests run: 0` — the vintage engine dependency is missing; re-check `pom.xml`.
3. **App boots:** `./mvnw spring-boot:run` and confirm the banner reports `Spring Boot v3.5.14`, server starts on port 5000, no `ClassNotFoundException` for `javax.servlet.*` in the log.
4. **Smoke test the request flow** in a browser at `http://localhost:5000/`:
   - `/` redirects to `/game.do`.
   - The game page loads, the welcome banner types out (confirms `gameJoin.json` works under jakarta-namespace Servlet API).
   - Submit a name; confirm a ship is created and the command prompt appears (`gameLogin.json` exercises `HttpSession` under jakarta).
   - Submit a command (e.g. `A1,5` to accelerate); confirm the response cycle works (`gameCommands.json` exercises `request.getParameter` and `playerSync()`).
   - Submit `H` for help; confirm the long help text streams.
   - Submit `STOP`; confirm the ship is destroyed and `session.invalidate()` fires without error.
   - Browse to `/admin.do`; confirm the uptime page renders (Thymeleaf 3.1 sanity check).
5. **Confirm `bin/` is gone:** `ls bin 2>/dev/null` should return nothing. `git status` should show the `bin/` deletion staged.
6. **No `javax.servlet` leakage:** `grep -rn 'javax.servlet' src/` should return zero matches.
7. **Dependency CVE sanity check (optional but recommended):** `./mvnw org.owasp:dependency-check-maven:check` should now show dramatically fewer critical/high findings. Capture the report before and after for the PR description.

## Out of Scope (Tracked Separately)

- Adding Spring Security 6, CSRF protection, session fixation rotation, and cookie hardening (security findings #4, #5, #6, #7).
- Removing `eval()` and replacing jQuery 1.3.2 (security findings #2, #3, #8).
- Sanitizing log inputs, fixing `validateStopCommand`, removing diagnostic endpoints (security findings #9, #10, #11).
- Migrating tests from JUnit 4 to Jupiter 5.
- Adding a `Dockerfile`, GitHub Actions CI, or Dependabot config.

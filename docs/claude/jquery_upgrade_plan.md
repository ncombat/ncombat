# jQuery 1.3.2 → 3.7.1 Upgrade Plan (NCombat)

## Context

`docs/claude/fix_jquery_prompt.md` asks for a plan to upgrade jQuery to **3.7.1** that also resolves the front-end findings of `docs/claude/security_analysis.md`. The current frontend is small:

- **One vendored jQuery copy**: `src/main/resources/static/html/lib/jquery-1.3.2.js` (June 2009; security finding #2 — multiple known XSS / prototype-pollution CVEs).
- **Three Thymeleaf templates** load it: `templates/ncombat.html:7`, `templates/ncombat2.html:7`, `templates/admin.html:7`.
- **Two custom JS utilities** that do not import jQuery: `static/html/lib/teletype.js`, `static/html/lib/ncombat_base.js`.
- **One orphan design-lab page** that loads jQuery 1.5.2 from `ajax.googleapis.com`: `static/html/content/docs/cominfo.html`.
- **Inline scripts in the three main templates** add up to ~13 unique jQuery API calls (`$.ajax`, `$.getJSON`, `.css`, `.html`, `.val`/`.attr`, `.focus`, `.click`, `.toggle`, `.load`, `$(document).ready`) plus three `eval(...)` JSON-parse sites (finding #3) and a Google Analytics block using `document.write` (finding #14).

The codebase is server-side Spring Boot 3.5.14 (recently upgraded) with no npm/webpack/TS tooling — static assets are served straight from `src/main/resources/static/`. There are no front-end tests. This means **the entire upgrade is a flat-file swap plus three template edits and one teletype.js rewrite** — no build-system work required.

Design choices (confirmed with the user):
- **Delivery**: vendored file (drop-in replacement; minimal pom/build impact).
- **Google Analytics**: remove entirely (resolves finding #14; the legacy `_gat`/`ga.js` API was deprecated by Google in 2012).
- **teletype.js**: switch to jQuery `.text()` / `.append(textNode)` idiom for the typewriter writes.
- **cominfo.html**: keep but update its jQuery to 3.7.1 (vendored, same as the main templates — also drop the googleapis CDN dependency).

## Verified Inventory (read directly, not from agents)

| File | Lines | jQuery sinks of interest |
|---|---|---|
| `templates/ncombat.html` | 7, 21, 24, 30–31, 34, 39, 52–58, 54, 62–83, 65, 68, 70–73, 77–82, 110–121 | jQuery load; `eval()` of response; `.css()`; `.val()` + `.focus()`; `.css()`; `.html(data.prompt)`; `$.ajax`; `.attr('value')`; `$(document).ready`; `$.getJSON`; `.load`; `.click` + `.toggle`; `$.ajax`; GA `document.write` |
| `templates/ncombat2.html` | identical to ncombat.html except 68 (`#dd` instead of `#docs`), 70 (`$("dt").click(... $(this).next().toggle())`) | same as above |
| `templates/admin.html` | 7, 19, 22, 27–28, 32, 45–51, 47, 55 | jQuery load; `eval()` of response; `.css()`; `.val()` + `.focus()`; `.html(data.prompt)`; `$.ajax`; `.attr('value')`; empty ready handler |
| `static/html/lib/teletype.js` | 43–49 | `ttElem.innerHTML = ttElem.innerHTML + msg;` (per-character typewriter) — finding #8 sink |
| `static/html/lib/ncombat_base.js` | — | No jQuery; uses `document.createTextNode` / `appendChild`. Cookie helpers + dice utilities. **No changes required** by this plan. |
| `static/html/content/docs/cominfo.html` | 9, 12–32 | Loads jQuery 1.5.2 from `ajax.googleapis.com`; inline script uses `.click`, `.html`, `.css`, `.attr`, `.slideDown/Up`, `.show`, `.find`, `.siblings`, `.next`, `.is(":visible")` |

## Changes (file by file)

### 1. Replace the vendored jQuery file

**Add** `src/main/resources/static/html/lib/jquery-3.7.1.min.js` (download from `https://code.jquery.com/jquery-3.7.1.min.js`). Verify the SHA-256 against the integrity value published on `https://releases.jquery.com/` at download time — **do not trust an integrity value quoted here from training data; the implementer must fetch the current value from the official site**. Suggested command:

```
curl -fsSL -o src/main/resources/static/html/lib/jquery-3.7.1.min.js https://code.jquery.com/jquery-3.7.1.min.js
openssl dgst -sha256 -binary src/main/resources/static/html/lib/jquery-3.7.1.min.js | openssl base64 -A
# Compare the printed base64 against the SHA-256 integrity published on https://releases.jquery.com/
```

**Delete** `src/main/resources/static/html/lib/jquery-1.3.2.js` (`git rm`).

### 2. Update the three main templates

In all three of `templates/ncombat.html`, `templates/ncombat2.html`, `templates/admin.html` (and analogous lines):

**Line 7 — change the `<script src>`:**
```html
<script type="text/javascript" src="html/lib/jquery-3.7.1.min.js"></script>
```

**Replace the `eval()` JSON-parse and `.attr('value')` patterns.** Switch each `$.ajax` block from `complete: processResponse` to `success`/`error` with `dataType: 'json'`, and switch `$('#cmds').attr('value')` to `$('#cmds').val()`. Refactored `processResponse` and `sendRequest` in `ncombat.html` / `ncombat2.html`:

```html
<script>
  ncombat = {};

  function processResponse(response) {
      var data = response.data;
      var messages = data.messages;
      if (messages) {
          $('#commandConsole').css('display', 'none');
          for (var i = 0; i < messages.length; i++) {
              teletype.println(messages[i]);
          }
          teletype.callWhenDone(function () {
              if (data.alive) {
                  $('#commandConsole').css('display', 'inline');
                  $('#cmds').val('').focus();
              } else {
                  $('#restartPane').css('display', 'inline-block');
              }
          });
      }
      if (data.prompt) {
          $('#prompt').text(data.prompt);         // .text(), not .html() — finding #8 defense-in-depth
      }
      if (data.url) {
          ncombat.nextUrl = data.url;
      }
  }

  function processError(xhr, status, error) {
      alert("AJAX ERROR: status='" + status + "', error='" + error + "'");
  }

  function sendRequest() {
      if (ncombat.nextUrl) {
          $.ajax({
              url: ncombat.nextUrl,
              type: 'post',
              dataType: 'json',
              data: { text: $('#cmds').val() },   // .val(), not .attr('value') — jQuery 3 breaking change
              success: processResponse,
              error: processError
          });
      }
  }

  $(function () {
      setInterval(function () { $.getJSON("gamePing.json"); }, 30000);
      $("#docs").load("html/content/docs/COMINFO.txt");     // ncombat2.html: $("#dd")
      $("#docsToggle").on('click', function () {            // ncombat2.html: $("dt").on('click', function() { $(this).next().toggle(); })
          $("#docs").toggle();
      });
      teletype.start(statusScreen);
      $.ajax({
          url: 'gameJoin.json',
          type: 'post',
          dataType: 'json',
          success: processResponse,
          error: processError
      });
  });
</script>
```

`admin.html` gets the same `processResponse` / `sendRequest` shape (without the `setInterval`, `.load`, `.click`, or `teletype.start` calls — its `$(document).ready` body is empty today).

**Delete the Google Analytics block** in `ncombat.html:110-121` and `ncombat2.html:110-121` — the entire `<div id="googleanalytics">` and its two inline `<script>` children. This removes the `document.write(unescape(...))` mixed-content load (finding #14).

**Notes on the jQuery 3 changes that matter here** (these are verified against jQuery 1.x → 3.x migration patterns generally, but the implementer should sanity-check against the current jQuery 3 release notes at https://jquery.com/ ):
- `.attr('value')` on a text input no longer reads the live value in 3.x — use `.val()`. **This is a behavioral break that would silently submit empty strings if left unchanged.**
- `.click(fn)` still works in 3.x but is on the deprecation track; `.on('click', fn)` is the recommended form.
- `$.ajax` with `dataType: 'json'` auto-parses the response and passes the parsed object to `success` — this replaces the `eval(xhr.responseText + '.data')` pattern with no extra ceremony.
- `$(fn)` is the shorthand for `$(document).ready(fn)`; both still work.
- `$.getJSON`, `.load`, `.toggle`, `.css`, `.html`, `.text`, `.focus`, `.val` — all still present and behaviorally compatible.

### 3. Rewrite `teletype.js` to drop `innerHTML`

Replace the `render` function (currently lines 43–53). Use jQuery `.append()` with text nodes / `<br>` elements, and switch the size-cap logic from `innerHTML.length` to a text-character counter so multi-byte HTML entities don't skew the cap.

```js
var charCount = 0;

var clip = function () {
    while (charCount > CLIPSIZE && ttElem.firstChild) {
        var node = ttElem.firstChild;
        if (node.nodeType === Node.TEXT_NODE) {
            charCount -= node.nodeValue.length;
        }
        ttElem.removeChild(node);
    }
};

var render = function (msg) {
    if (running) {
        if (msg === NEWLINE) {                           // '<br>' sentinel
            $(ttElem).append('<br>');                    // 1-element safe markup
        } else if (msg === SPACE) {                      // '&nbsp;' sentinel
            $(ttElem).append(document.createTextNode(' '));
            charCount += 1;
        } else {
            $(ttElem).append(document.createTextNode(msg));
            charCount += msg.length;
        }
        if (charCount > MAXSIZE) clip();
        ttElem.scrollTop = ttElem.scrollHeight;
    } else {
        holdBuffer += msg;
    }
};
```

The `NEWLINE` and `SPACE` constants (lines 2–3) still work as sentinels for `typeCharacter` to detect; they no longer get re-parsed as HTML downstream. Net effect: every byte that comes out of the server is rendered as **text**, never as HTML — finding #8 closed at the front-end sink regardless of what the server sends.

`pause()` (line 70) clears `holdBuffer`. If a `pause()` + `resume()` cycle is exercised in practice (does not appear to be from the inline scripts), confirm `holdBuffer` is initialised to `""` not `undefined` — the existing code has the same latent bug. Out of scope for this plan; flag in the PR description.

### 4. Update `cominfo.html`

`static/html/content/docs/cominfo.html:9` currently loads `https://ajax.googleapis.com/ajax/libs/jquery/1.5.2/jquery.min.js`. Replace with the vendored 3.7.1:

```html
<script type="text/javascript" src="../../lib/jquery-3.7.1.min.js"></script>
```

The inline script in this file uses `.click`, `.html`, `.css`, `.attr`, `.show`, `.slideDown/Up`, `.find`, `.siblings`, `.next`, `.is(":visible")` — all still supported in jQuery 3.7.1 with identical semantics. **No further edits required** to cominfo.html beyond the `<script src>` line.

This file is not linked from any other template (the main templates load `COMINFO.txt`, not `cominfo.html`), but Spring Boot still serves it at `/html/content/docs/cominfo.html`, so leaving an old jQuery in place would keep an attack surface alive. Updating it to 3.7.1 closes the gap without adding maintenance burden.

## Security Findings Resolved by This Plan

Mapped against `docs/claude/security_analysis.md`:

| # | Severity | Title | Status after this PR |
|---|---|---|---|
| 2 | High | jQuery 1.3.2 — multiple known XSS CVEs | **Resolved** — vendored jQuery 3.7.1 replaces 1.3.2 in three templates and cominfo.html. |
| 3 | High | `eval()` of server response in Ajax handlers | **Resolved** — three `eval(...)` sites replaced with `dataType: 'json'` + parsed `response.data`. |
| 8 | Medium | Player-supplied text rendered via `$.html()` and `innerHTML` without sanitization | **Resolved at the front-end sinks**: `.html(data.prompt)` → `.text(data.prompt)` in all three templates; `teletype.js` `innerHTML +=` replaced with text-node appends. Server-side escaping of player names/messages is still recommended as defense-in-depth (out of scope here). |
| 14 | Low | Google Analytics over mixed content via `document.write` | **Resolved** — entire `<div id="googleanalytics">` block deleted from both templates. |

**Not addressed (back-end, out of scope of this prompt):** #1 (Spring Boot — already done in a previous PR), #4 (admin auth), #5 (CSRF), #6 (session fixation), #7 (cookie attrs / HTTPS), #9 (log injection), #10 (test endpoints), #11 (`validateStopCommand`), #12 (cache race), #13 (rate limiting), #15 (`bin/` module), #16 (`Math.random()`).

## Dependency / Supply-chain Verification

The prompt requires: *"Validate that the planned solution does not introduce dependencies with security vulnerabilities."*

- **No new Maven dependencies are added.** `pom.xml` is unchanged. Vendoring jQuery as a static file does not introduce any transitive supply chain.
- **One Maven dependency is implicitly removed from the runtime page**: the googleapis CDN load of jQuery 1.5.2 from `cominfo.html`. This removes a third-party load surface.
- **jQuery 3.7.1 known CVEs at training cutoff (August 2025):** I am not aware of an unpatched CVE against 3.7.1 itself. **This is past my verifiable cutoff; the implementer should run `./mvnw org.owasp:dependency-check-maven:check` (which won't see the JS file) AND cross-check `https://github.com/jquery/jquery/security` and `https://snyk.io/vuln/npm:jquery` for any advisory against 3.7.1 published between August 2025 and the implementation date.** If a 3.7.x successor (e.g. 3.7.2) has shipped by then, pin to the latest patch on the 3.x line.
- **No `eval`, `Function()` constructor, or `unsafe-eval` requirement remains** in the post-change frontend — this also unblocks a future `Content-Security-Policy: script-src 'self'` header without `unsafe-eval` (recommended in security_analysis.md but not part of this PR).

## Verification

Run sequentially after the edits:

1. **Build clean:** `./mvnw clean package` — no changes to Java, so the existing 69-test suite must still pass with zero failures (the regression baseline from `spring_upgrade_plan.md`).
2. **App boots:** `./mvnw spring-boot:run`. Confirm the server starts on port 5000 with no template-parse warnings.
3. **Browser smoke test at `http://localhost:5000/`** — exercise the full request cycle. The success criterion for the upgrade is **byte-for-byte identical user-visible behavior** to today, with no console warnings:
   - `/` redirects to `/game.do`.
   - The status banner types out via teletype (confirms `gameJoin.json` round-trip and the rewritten `render`).
   - `ENTER YOUR NAME?` prompt renders (confirms `data.prompt` flow through `.text()`).
   - Submit a name → ship is created, `CMDS?` prompt appears (confirms `$.ajax` success-callback path and `.val()` reads the input correctly — **this is where a missed `.attr('value')` → `.val()` change would manifest as an empty submission**).
   - Submit `A1,5` → acceleration command executes.
   - Submit `H` → help text streams via the new typewriter (confirms `<br>` handling and the text-node truncation logic).
   - Submit `STOP` → ship is destroyed, restart pane appears.
   - Refresh the page; wait 30+ seconds; verify a `gamePing.json` request in DevTools Network tab (confirms `$.getJSON` still fires).
   - Click the docs toggle (`#docsToggle` on ncombat.html, `<dt>` on ncombat2.html) → docs panel expands/collapses (confirms `.on('click', ...)` + `.toggle()`).
4. **`/admin.do` smoke test:** navigate to `http://localhost:5000/admin.do`. Page should render with uptime; no JS console errors (the rewritten `processResponse` and `sendRequest` are loaded but not exercised because the inline script's `$(document).ready` body is empty).
5. **DevTools console:** must be free of `Uncaught TypeError`, `Uncaught ReferenceError`, and jQuery `Migrate` warnings (the jQuery-migrate plugin is NOT added; if any deprecation surfaces, it surfaces as a `console.warn` from our own code). **Empty console = pass.**
6. **DevTools Network tab:** confirm no request goes to `google-analytics.com` or `ajax.googleapis.com` from any page in the app.
7. **Confirm old file is gone:** `ls src/main/resources/static/html/lib/jquery-1.3.2.js 2>/dev/null` should return nothing; `git status` should show the deletion staged.
8. **Confirm no `eval` survives:** `grep -rn 'eval(' src/main/resources/templates/` should return zero matches.
9. **Confirm no `document.write` survives:** `grep -rn 'document.write' src/main/resources/` should return zero matches.

If any of (3)–(6) fails, the per-template script block in (2) is the likeliest culprit — diff against the snippet in §2.

## Permissions to Request Up Front (unattended execution)

Per the prompt's "Unattended Operation" clause. Recommended `.claude/settings.local.json` allow-list to add before kicking off the implementation (or grant interactively at the first prompt):

- `Bash(curl:*)` — fetch jQuery 3.7.1 from `code.jquery.com`.
- `Bash(openssl:*)` — verify the SHA-256.
- `Bash(./mvnw:*)` — build, test, and run the app.
- `Bash(grep:*)`, `Bash(ls:*)`, `Bash(git status:*)`, `Bash(git diff:*)`, `Bash(git rm:*)` — verification and staging.
- `Edit`, `Write` on `src/main/resources/templates/*.html`, `src/main/resources/static/html/lib/*.js`, `src/main/resources/static/html/content/docs/cominfo.html`.
- `WebFetch(domain:jquery.com)`, `WebFetch(domain:releases.jquery.com)` — if checking the current published SHA / advisories at run time.

`./mvnw spring-boot:run` should be launched in the background (so Bash returns promptly) and stopped by killing the process at the end of the smoke test.

## Out of Scope (followups, intentionally not done here)

- Adding a `Content-Security-Policy` header (`default-src 'self'; script-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'`). Eliminating `eval()` is a prerequisite that this PR ships; the actual header belongs in a Spring `Filter` / Spring Security config in a separate PR.
- Server-side HTML-escaping of player names and chat messages (the *source* of the text that `teletype.js` renders). This PR closes the front-end sink; the server-side defense-in-depth belongs with the broader auth/sanitization PR.
- Replacing jQuery entirely with vanilla `fetch` + `Element.textContent`. The security analysis flagged this as preferred; the prompt's "minimize learning required" constraint argued against it. Leave the door open by keeping the rewritten code small and idiomatic.
- Removing the latent `holdBuffer` initialisation bug in `teletype.js:pause()` (out of scope; flag in PR description).
- Cleaning up `ncombat_base.js` (uses 1990s-era cookie helpers and a `diceRoll` that returns wrong arithmetic — not exercised by current pages, leave for a separate cleanup).
# Prompt for Fixing jQuery

## Goal

Plan the upgrade of jQuery to version 3.7.1.

## Details

- Plan the code changes necessary for the upgrade.
- Try to minimize the amount of boilerplate necessary.
- Assume the people implementing this refactoring are senior software engineers, generally, and are familiar with, 
  but not necessarily experts in, web front-end technologies.
- Try to minimize the amount of learning such engineers will require to get the job done.

## Constraints

- Try to resolve all front-end security issues.
- Try to resolve all front-end issues laid out in security_analysis.md.

## Verification

- Validate that the planned solution does not introduce dependencies with security vulnerabilities.

## Output

- Display your results on the console.
- Also write the results to a new file, /docs/claude/ui_refactoring_jquery_recommendations.md.

## Reference

- Study the jQuery documentation found at https://jquery.com/ .
- Do not consider the file ui_refactoring_recommendations.md in this analysis.

## Unattended Operation

- This operation will be run largely unattended.
- Try to ask for necessary permissions up front, rather than intermittently, so that the bulk of the work can be
  completed unattended.
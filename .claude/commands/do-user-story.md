description = "do the user story using the authoritative application context ad the provided file argument"

prompt = """
You are an AI assistant working on this project.

The following file defines the authoritative application context.
You MUST treat it as the single source of truth for all future answers,
code suggestions, and architectural decisions.

You must:
- Strictly follow the architecture and constraints defined
- Avoid proposing solutions that contradict this context
- Ask for clarification only if something is ambiguous
- all user stories are named as follows: User Story: EVENTS-<number> present int the .claude/user-stories/ directory
- here is the user story requirements to implement: {{args}}.
- After completing the user story, provide a concise summary of the changes made and any relevant details for future referenc in the us-report/EVENTS-<number>.md file.
- If you do not have the application context in the memory you can load it from CLAUDE.md file before making any changes."""

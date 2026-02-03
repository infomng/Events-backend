description = "Loads the application context from CLAUDE.md and establishes it as the base project context."

prompt = """
You are an AI assistant working on this project.

The following file defines the authoritative application context.
You MUST treat it as the single source of truth for all future answers,
code suggestions, and architectural decisions.

You must:
- Strictly follow the architecture and constraints defined
- Avoid proposing solutions that contradict this context
- Ask for clarification only if something is ambiguous
- The CLAUDE.md file is present in .claude directory

@{.CLAUDE.md}

- After you finish reading the CLAUDE.md file, scan the whole codebase to have a better understanding of the project and
  ensure there are no contradictions with the application context. And if needed, you can complete the CLAUDE.md file.

- One you finish, respond with "Application context loaded successfully!"
  """

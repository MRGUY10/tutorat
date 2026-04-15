/**
 * Default avatar image for users without a profile photo
 * This is a base64-encoded SVG that can be used as an image src
 */
export const DEFAULT_AVATAR = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiBmaWxsPSIjNkI3MjgwIi8+CjxjaXJjbGUgY3g9IjEwMCIgY3k9IjgwIiByPSI0MCIgZmlsbD0iI0Q5RDlEOSIvPgo8cGF0aCBkPSJNNDAgMTYwQzQwIDEzNS4xNDcgNjUuMTQ3MiAxMTUgMTAwIDExNUMxMzQuODUzIDExNSAxNjAgMTM1LjE0NyAxNjAgMTYwVjIwMEg0MFYxNjBaIiBmaWxsPSIjRDlEOUQ5Ii8+Cjwvc3ZnPg==';

/**
 * Helper function to get avatar image URL
 * Returns the user's photo if available, otherwise returns the default avatar
 */
export function getAvatarUrl(photoUrl?: string | null): string {
  return photoUrl || DEFAULT_AVATAR;
}

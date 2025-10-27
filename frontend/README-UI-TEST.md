# UI Test Mode

This directory contains a script to run the frontend in UI test mode.

## What is UI Test Mode?

When `REACT_APP_UI_TEST=true` is set, the frontend uses static mock data instead of making API calls to the backend. This allows you to test all UI features without needing the backend server running.

## How to Use

Run the provided script:

```bash
./start-ui-test.sh
```

Or manually:

```bash
REACT_APP_UI_TEST=true npm start
```

## Mock Data

The mock data includes:

- 2 sample contests (one as Owner, one as Editor)
- Tasks within each contest
- All API operations (create, update, delete, reorder) work with the mock data

## Disabling UI Test Mode

Remove or set `REACT_APP_UI_TEST=false` in the `.env` file, or run without the environment variable.

module.exports = {
    addons: [
        '@storybook/addon-actions/register',
        '@storybook/addon-knobs/register',
        '@storybook/addon-a11y/register',
    ],
    stories: ['../src/**/*.stories.tsx'],
    webpackFinal: async (config) => {
        config.module.rules.push({
            test: /\.(ts|tsx)$/,
            loader: require.resolve('react-docgen-typescript-loader'),
        });
        config.resolve.extensions.push('.ts', '.tsx');
        return config;
    },
};

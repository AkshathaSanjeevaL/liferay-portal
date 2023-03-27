import {useEffect, useState} from 'react';

import accountLogo from '../../assets/icons/mainAppLogo.svg';
import {
	AppProps,
	DashboardTable,
} from '../../components/DashboardTable/DashboardTable';
import {PublishedAppsDashboardTableRow} from '../../components/DashboardTable/PublishedAppsDashboardTableRow';
import {getProductSpecifications, getProducts, getUserAccounts} from '../../utils/api';
import {DashboardPage, DashboardListItems} from '../DashBoardPage/DashboardPage';
import {initialDashboardNavigationItems, MemberProps} from './PublishedDashboardPageUtil';
import {DashboardMemberTableRow} from '../../components/DashboardTable/DashboardMemberTableRow';

declare let Liferay: {ThemeDisplay: any; authToken: string};

const appTableHeaders = [
	{
		iconSymbol: 'order-arrow',
		title: 'Name',
	},
	{
		title: 'Version',
	},
	{
		title: 'Type',
	},
	{
		title: 'Last Updated',
	},
	{
		title: 'Status',
	},
];

const memberTableHeaders = [
	{
		iconSymbol: 'order-arrow',
		title: 'Name',
	},
	{
		title: 'Email',
	},
	{
		title: 'Role',
	},
];

export function PublishedAppsDashboardPage() {
	const [apps, setApps] = useState<AppProps[]>(Array<AppProps>());
	const [dashboardNavigationItems, setDashboardNavigationItems] = useState(
		initialDashboardNavigationItems
	);
	const [selectedNavigationItem, setSelectedNavigationItem] = useState('Apps');
	const [members, setMembers] = useState<MemberProps[]>(Array<MemberProps>());

	const appMessages = {
		description: 'Manage and publish apps on the Marketplace',
		emptyStateMessage: {
			description1: 'Publish apps and they will show up here.',
			description2: 'Click on “New App” to start.',
			title: 'No apps yet',
		},
		title: 'Apps',
	};

	const memberMessages = {
		description: 'Manage users in your development team and invite new ones',
		emptyStateMessage: {
			description1: 'Create new members and they will show up here.',
			description2: 'Click on “New Member” to start.',
			title: 'No members yet',
		},
		title: 'Members',
	};

	const formatDate = (date: string) => {
		const locale = Liferay.ThemeDisplay.getLanguageId().replace('_', '-');

		const dateOptions: any = {
			day: 'numeric',
			month: 'short',
			year: 'numeric',
		};

		const formattedDate = new Intl.DateTimeFormat(
			locale,
			dateOptions
		).format(new Date(date));

		return formattedDate;
	};

	function getAppListProductSpecifications(productIds: number[]) {
		const appListProductSpecifications: any[] = [];

		productIds.forEach((productId) => {
			appListProductSpecifications.push(
				getProductSpecifications({appProductId: productId})
			);
		});

		return Promise.all(appListProductSpecifications);
	}

	function getAppListProductIds(products: any) {
		const productIds: any[] = [];

		products.items.map((product: any) => {
			productIds.push(product.productId);
		});

		return productIds;
	}

	function getProductTypeFromSpecifications(specifications: any) {
		let productType = 'no type';

		specifications.items.forEach((specification: any) => {
			if (specification.specificationKey === 'type') {
				productType = specification.value.en_US;

				if (productType === 'cloud') {
					productType = 'Cloud';
				}
				else if (productType === 'osgi') {
					productType = 'OSGI';
				}
			}
		});

		return productType;
	}

	function getProductVersionFromSpecifications(specifications: any) {
		let productVersion = '0';

		specifications.items.forEach((specification: any) => {
			if (specification.specificationKey === 'version') {
				productVersion = specification.value.en_US;
			}
		});

		return productVersion;
	}

	useEffect(() => {
		(async () => {
			const appList = await getProducts();

			const appListProductIds: number[] = getAppListProductIds(appList);

			const appListProductSpecifications =
				await getAppListProductSpecifications(appListProductIds);

			const newAppList = appList.items.map(
				(product: any, index: number) => {
					return {
						externalReferenceCode: product.externalReferenceCode,
						lastUpdatedBy: product.lastUpdatedBy,
						name: product.name.en_US,
						productId: product.productId,
						status: product.workflowStatusInfo.label.replace(
							/(^\w|\s\w)/g,
							(m: string) => m.toUpperCase()
						),
						thumbnail: product.thumbnail,
						type: getProductTypeFromSpecifications(
							appListProductSpecifications[index]
						),
						updatedDate: formatDate(product.modifiedDate),
						version: getProductVersionFromSpecifications(
							appListProductSpecifications[index]
						),
					};
				}
			);

			const currentAppNavigationItem = dashboardNavigationItems.find((navigationItem) => navigationItem.itemName === 'apps') as DashboardListItems;

			const newAppNavigationItem = {
                ...currentAppNavigationItem,
                items: newAppList,
            }

			setDashboardNavigationItems([
				newAppNavigationItem,
                ...dashboardNavigationItems.filter((navigationItem) => navigationItem.itemName !== 'apps')
            ]);

			setApps(newAppList);
		})();
	}, []);

	useEffect(() => {
		(() => {
			const clickedNavigationItem: any = dashboardNavigationItems.find(
				dashboardNavigationItem => dashboardNavigationItem.itemSelected
			);

			setSelectedNavigationItem(clickedNavigationItem.itemTitle);
		})();
	}, [dashboardNavigationItems]);

	useEffect(() => {
		(async () => {
			if (selectedNavigationItem === "Members") {

				const accountsListResponse = await getUserAccounts();

				const membersList = accountsListResponse.items.map((account: any) => {
					return {
						name: account.name,
						email: account.emailAddress,
						image: account.image,
						role: account.roleBriefs[0].name,
						dateCreated: account.dateCreated,
						lastLoginDate: account.lastLoginDate,
						userId: account.id
					}
				})

				setMembers(membersList);
			}
		})();
	}, [selectedNavigationItem]);

	return (
		<div>
			{(() => {
				if (selectedNavigationItem === "Apps") {
					return (
						<DashboardPage
							accountAppsNumber="4"
							accountLogo={accountLogo}
							accountTitle="Acme Co"
							buttonMessage="+ New App"
							dashboardNavigationItems={dashboardNavigationItems}
							messages={appMessages}
							setDashboardNavigationItems={setDashboardNavigationItems}
						>
							<DashboardTable<AppProps>
								emptyStateMessage={appMessages.emptyStateMessage}
								items={apps}
								tableHeaders={appTableHeaders}
							>
								{(item) => (
									<PublishedAppsDashboardTableRow
										item={item}
										key={item.name}
									/>
								)}
							</DashboardTable>
						</DashboardPage>
					)
				} else if (selectedNavigationItem === "Members") {
					return (
						<DashboardPage
							accountAppsNumber="4"
							accountLogo={accountLogo}
							accountTitle="Acme Co"
							dashboardNavigationItems={dashboardNavigationItems}
							messages={memberMessages}
							setDashboardNavigationItems={setDashboardNavigationItems}
						>
							<DashboardTable<MemberProps>
								emptyStateMessage={memberMessages.emptyStateMessage}
								items={members}
								tableHeaders={memberTableHeaders}
							>
								{(item) => (
									<DashboardMemberTableRow
										item={item}
										key={item.name}
									/>
								)}
							</DashboardTable>
						</DashboardPage>
					)
				}
			})()}
		</div>
	);
}
